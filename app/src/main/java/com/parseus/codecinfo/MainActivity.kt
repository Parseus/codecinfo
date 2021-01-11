package com.parseus.codecinfo

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.databinding.ActivityMainBinding
import com.parseus.codecinfo.fragments.CodecDetailsFragment
import com.parseus.codecinfo.settings.DarkTheme
import com.parseus.codecinfo.settings.SettingsContract
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var shouldRecreateActivity = false

    private val useImmersiveMode: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("immersive_mode", true)

    private val settingsContract = registerForActivityResult(SettingsContract()) { result ->
        shouldRecreateActivity = result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 21) {
            val reenter = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                excludeTarget(android.R.id.statusBarBackground, true)
                excludeTarget(android.R.id.navigationBarBackground, true)
            }
            val exit = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                excludeTarget(android.R.id.statusBarBackground, true)
                excludeTarget(android.R.id.navigationBarBackground, true)
            }
            window.apply {
                requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                reenterTransition = reenter
                exitTransition = exit
            }
        }

        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val defaultThemeMode = getDefaultThemeOption(this)
        val darkTheme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("dark_theme", defaultThemeMode.toString())!!.toInt()
        AppCompatDelegate.setDefaultNightMode(DarkTheme.getAppCompatValue(darkTheme))

        setSupportActionBar(binding.toolbar)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        initializeAppRating(this)

        if (savedInstanceState != null) {
            recreateDetailFragmentIfNeedded()
        }
    }

    private fun recreateDetailFragmentIfNeedded() {
        supportFragmentManager.executePendingTransactions()
        val detailsFragment = supportFragmentManager.findFragmentByTag(getString(R.string.details_fragment_tag))
        detailsFragment?.let {
            val bundle = it.arguments
            supportFragmentManager.commit {
                remove(it)
            }

            supportFragmentManager.commit {
                if (isInTwoPaneMode()) {
                    supportFragmentManager.popBackStack()
                    replace(R.id.codecDetailsFragment, CodecDetailsFragment::class.java,
                            bundle, getString(R.string.details_fragment_tag))
                } else {
                    replace(R.id.content_fragment, CodecDetailsFragment::class.java,
                            bundle, getString(R.string.details_fragment_tag))
                    addToBackStack(null)

                    supportActionBar!!.apply {
                        if (displayOptions and ActionBar.DISPLAY_HOME_AS_UP == 0) {
                            setDisplayHomeAsUpEnabled(true)
                            setHomeActionContentDescription(R.string.close_details)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (shouldRecreateActivity) {
            recreate()
            return
        }
    }

    override fun recreate() {
        super.recreate()
        shouldRecreateActivity = false
    }

    override fun onDestroy() {
        super.onDestroy()
        destroySamsungGestures()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && useImmersiveMode) setImmersiveMode()
    }

    @Suppress("DEPRECATION")
    private fun setImmersiveMode() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                supportFragmentManager.popBackStack()
            }

            R.id.menu_item_share -> {
                val detailsFragment = supportFragmentManager.findFragmentByTag(
                        getString(R.string.details_fragment_tag)) as? CodecDetailsFragment
                val codecShareOptions = if (detailsFragment != null) {
                    arrayOf(
                            getString(R.string.codec_list),
                            getString(R.string.codec_all_info),
                            getString(R.string.codec_details_selected))
                } else {
                    arrayOf(
                            getString(R.string.codec_list),
                            getString(R.string.codec_all_info))
                }

                var codecId = ""
                var codecName = ""

                detailsFragment?.let { fragment ->
                    if (fragment.isVisible) {
                        codecId = fragment.codecId
                        codecName = fragment.codecName
                    }
                }

                MaterialAlertDialogBuilder(this).run {
                    setTitle(R.string.choose_share)
                    setSingleChoiceItems(codecShareOptions, -1) { dialog, option ->
                        launchShareIntent(option, codecId, codecName)
                        dialog.dismiss()
                    }
                    show()
                }

                return true
            }

            R.id.menu_item_settings -> settingsContract.launch(Unit)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun launchShareIntent(option: Int, codecId: String, codecName: String) {
        val codecStringBuilder = StringBuilder()

        when (option) {
            0 -> {
                codecStringBuilder.append("${getString(R.string.codec_list)}:\n\n")
                val codecSimpleInfoList = getSimpleCodecInfoList(this, true)
                codecSimpleInfoList.addAll(getSimpleCodecInfoList(this, false))

                for (info in codecSimpleInfoList) {
                    codecStringBuilder.append("$info\n")
                }
            }

            1 -> {
                codecStringBuilder.append("${getString(R.string.codec_list)}:\n")
                val codecSimpleInfoList = getSimpleCodecInfoList(this, true)
                codecSimpleInfoList.addAll(getSimpleCodecInfoList(this, false))

                for (info in codecSimpleInfoList) {
                    codecStringBuilder.append("\n$info\n")
                    val codecInfoMap = getDetailedCodecInfo(this, info.codecId, info.codecName)

                    for (key in codecInfoMap.keys) {
                        val stringToAppend = if (key != getString(R.string.bitrate_modes)
                            && key != getString(R.string.color_profiles)
                            && key != getString(R.string.profile_levels)
                            && key != getString(R.string.max_frame_rate_per_resolution)) {
                            "$key: ${codecInfoMap[key]}\n"
                        } else {
                            "$key:\n${codecInfoMap[key]}\n"
                        }
                        codecStringBuilder.append(stringToAppend)
                    }
                }
            }

            2 -> {
                val codecInfoMap = getDetailedCodecInfo(this, codecId, codecName)
                codecStringBuilder.append("${getString(R.string.codec_details)}: $codecName\n\n")

                for (key in codecInfoMap.keys) {
                    val stringToAppend = if (key != getString(R.string.bitrate_modes)
                            && key != getString(R.string.color_profiles)
                            && key != getString(R.string.profile_levels)
                            && key != getString(R.string.max_frame_rate_per_resolution)) {
                        "$key: ${codecInfoMap[key]}\n"
                    } else {
                        "$key:\n${codecInfoMap[key]}\n"
                    }
                    codecStringBuilder.append(stringToAppend)
                }
            }
        }

        val shareIntent = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, codecStringBuilder.toString())

            val title = if (option != 2) {
                getString(R.string.codec_list)
            } else {
                "${getString(R.string.codec_details)}: $codecName"
            }

            putExtra(Intent.EXTRA_TITLE, title)

            if (Build.VERSION.SDK_INT >= 29) {
                storeInfoIconForShare()?.let {
                    clipData = it
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            }

        }, null)
        startActivity(shareIntent)
    }

    @TargetApi(29)
    private fun storeInfoIconForShare(): ClipData? {
        return try {
            val iconFile = File(filesDir, INFO_ICON_FILE_NAME)
            val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_info) as VectorDrawable
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.setTint(getAttributeColor(com.google.android.material.R.attr.colorPrimary))
            drawable.draw(canvas)

            iconFile.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            val imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", iconFile)
            ClipData.newUri(contentResolver, null, imageUri)
        } catch (e: Exception) { null }
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == 29 && isTaskRoot && supportFragmentManager.backStackEntryCount == 0) {
            // Workaround for a memory leak from https://issuetracker.google.com/issues/139738913
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val INFO_ICON_FILE_NAME = "info_icon.png"
    }

}
