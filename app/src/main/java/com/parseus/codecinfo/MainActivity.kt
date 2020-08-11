package com.parseus.codecinfo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ShareCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.parseus.codecinfo.adapters.PagerAdapter
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.databinding.ActivityMainBinding
import com.parseus.codecinfo.fragments.CodecDetailsFragment
import com.parseus.codecinfo.settings.DarkTheme
import com.parseus.codecinfo.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var shouldRecreateActivity = false

    private val useImmersiveMode: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("immersive_mode", true)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val defaultThemeMode = getDefaultThemeOption()
        val darkTheme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("dark_theme", defaultThemeMode.toString())!!.toInt()
        AppCompatDelegate.setDefaultNightMode(DarkTheme.getAppCompatValue(darkTheme))

        setSupportActionBar(binding.toolbar)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        initializeAppRating(this)

        val tabs = binding.tabLayout
        val viewPager = binding.pager.apply {
            val pagerAdapter = PagerAdapter(this@MainActivity, supportFragmentManager)
            adapter = pagerAdapter
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        }
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab?.position ?: 0
            }
        })
        tabs.setupWithViewPager(viewPager)

        initializeSamsungGesture(this, viewPager, tabs)

        if (isInTwoPaneMode()) {
            return
        }

        if (savedInstanceState != null) {
            supportFragmentManager.executePendingTransactions()
            val fragmentById = supportFragmentManager.findFragmentById(R.id.codecDetailsFragment)
            fragmentById?.let { supportFragmentManager.beginTransaction().remove(fragmentById).commit() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_SETTINGS && data != null) {
            if (data.getBooleanExtra(SettingsActivity.FILTER_TYPE_CHANGED, false)
                    || data.getBooleanExtra(SettingsActivity.SORTING_CHANGED, false)
                    || data.getBooleanExtra(SettingsActivity.IMMERSIVE_CHANGED, false)) {
                shouldRecreateActivity = true
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (isInTwoPaneMode()) {
            supportFragmentManager.findFragmentByTag("SINGLE_PANE_DETAILS")?.let {
                (it as DialogFragment).dialog?.takeIf { dialog -> dialog.isShowing }?.dismiss()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && useImmersiveMode) setImmersiveMode()
    }

    @Suppress("DEPRECATION")
    private fun setImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
            R.id.menu_item_share -> {
                val fragmentById = supportFragmentManager.findFragmentById(R.id.codecDetailsFragment) as CodecDetailsFragment?
                val codecId = fragmentById?.codecId
                val codecName = fragmentById?.codecName

                val codecShareOptions = if (isInTwoPaneMode()
                        && (codecId != null && codecName != null)) {
                    arrayOf(
                            getString(R.string.codec_list),
                            getString(R.string.codec_all_info),
                            getString(R.string.codec_details_selected))
                } else {
                    arrayOf(
                            getString(R.string.codec_list),
                            getString(R.string.codec_all_info))
                }

                val builder = MaterialAlertDialogBuilder(this)
                var alertDialog: AlertDialog? = null
                builder.setTitle(R.string.choose_share)
                builder.setSingleChoiceItems(codecShareOptions, -1) { _, option ->
                    launchShareIntent(option)
                    alertDialog!!.dismiss()
                }
                alertDialog = builder.create()
                alertDialog.show()

                return true
            }
            R.id.menu_item_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java), OPEN_SETTINGS)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun launchShareIntent(option: Int) {
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
                val fragmentById = supportFragmentManager.findFragmentById(R.id.codecDetailsFragment) as CodecDetailsFragment
                val codecId = fragmentById.codecId
                val codecName = fragmentById.codecName

                val codecInfoMap = getDetailedCodecInfo(this, codecId!!, codecName!!)
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

        ShareCompat.IntentBuilder.from(this).setType("text/plain")
                .setText(codecStringBuilder.toString()).startChooser()
    }

    companion object {
        private const val OPEN_SETTINGS = 42
    }

}
