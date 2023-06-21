package com.parseus.codecinfo.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.SearchManager
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.kieronquinn.monetcompat.app.MonetCompatActivity
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.parseus.codecinfo.BuildConfig
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.audioCodecList
import com.parseus.codecinfo.data.codecinfo.videoCodecList
import com.parseus.codecinfo.data.drm.drmList
import com.parseus.codecinfo.data.knownproblems.DATABASES_INITIALIZED
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.databinding.ActivityMainBinding
import com.parseus.codecinfo.databinding.DeviceIssuesLayoutBinding
import com.parseus.codecinfo.ui.adapters.DeviceIssuesAdapter
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.ui.settings.DarkTheme
import com.parseus.codecinfo.ui.settings.SettingsContract
import com.parseus.codecinfo.utils.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dev.kdrag0n.monet.theme.ColorScheme
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import java.io.File
import java.util.*

class MainActivity : MonetCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding

    private var shouldRecreateActivity = false

    private val useImmersiveMode: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("immersive_mode", true)

    private val settingsContract = registerForActivityResult(SettingsContract()) { result ->
        shouldRecreateActivity = result
    }

    val searchListeners = mutableListOf<SearchView.OnQueryTextListener>()

    override val recreateMode: Boolean
        get() = Build.VERSION.SDK_INT >= 21 && !isNativeMonetAvailable()
    override val updateOnCreate: Boolean
        get() = Build.VERSION.SDK_INT >= 21 && !isNativeMonetAvailable()

    init {
        createInAppUpdateResultLauncher(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        disableApiBlacklistOnPie()

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

            installSplashScreen()
        }

        setTheme(R.style.Theme_CodecInfo)

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 21 && !isNativeMonetAvailable()) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    monet.awaitMonetReady()
                    initializeUI(savedInstanceState)
                    window.updateStatusBarColor(this@MainActivity)
                }
            }
        } else {
            initializeUI(savedInstanceState)
            window.updateStatusBarColor(this)
        }

        onBackPressedDispatcher.addCallback(this) {
            if (Build.VERSION.SDK_INT == 29 && isTaskRoot && supportFragmentManager.backStackEntryCount == 0) {
                // Workaround for a memory leak from https://issuetracker.google.com/issues/139738913
                finishAfterTransition()
            } else {
                if (!isInTwoPaneMode()) {
                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

    private fun initializeUI(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val defaultThemeMode = getDefaultThemeOption(this)
        val darkTheme = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("dark_theme", defaultThemeMode.toString())!!.toInt()
        AppCompatDelegate.setDefaultNightMode(DarkTheme.getAppCompatValue(darkTheme))

        setSupportActionBar(binding.toolbar)
        binding.toolbar.updateToolBarColor(this)

        binding.updateProgressBar?.updateColors(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        if (savedInstanceState != null) {
            recreateDetailFragmentIfNeedded()
        }

        handleIntent(intent)

        if (!DATABASES_INITIALIZED) {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, KnownProblem::class.java)
            val adapter = moshi.adapter<List<KnownProblem>>(type)

            try {
                resources.openRawResource(R.raw.known_problems_list).source().buffer().use {
                    KNOWN_PROBLEMS_DB = adapter.fromJson(it) ?: emptyList()
                }
            } catch (e: Exception) {
                KNOWN_PROBLEMS_DB = emptyList()
            }

            try {
                resources.openRawResource(R.raw.known_problems_list).source().buffer().use {
                    DEVICE_PROBLEMS_DB = adapter.fromJson(it) ?: emptyList()
                }
            } catch (e: Exception) {
                DEVICE_PROBLEMS_DB = emptyList()
            }

            DATABASES_INITIALIZED = true
        }

        if (!BuildConfig.DEBUG) {
            initializeAppRating(this)
            checkForUpdate(this, binding.updateProgressBar)
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
                    replace(R.id.itemDetailsFragment, DetailsFragment::class.java,
                            bundle, getString(R.string.details_fragment_tag))
                } else {
                    replace(R.id.content_fragment, DetailsFragment::class.java,
                            bundle, getString(R.string.details_fragment_tag))
                    addToBackStack(null)

                    supportActionBar!!.apply {
                        if (displayOptions and ActionBar.DISPLAY_HOME_AS_UP == 0) {
                            setHomeButtonEnabled(true)
                            setDisplayHomeAsUpEnabled(true)
                            setHomeActionContentDescription(R.string.close_details)
                        }
                    }
                }
            }
        }
    }

    override fun onMonetColorsChanged(monet: MonetCompat, monetColors: ColorScheme, isInitialChange: Boolean) {
        if (!isDynamicThemingEnabled(this) || isNativeMonetAvailable()) {
            return
        }
        super.onMonetColorsChanged(monet, monetColors, isInitialChange)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                onQueryTextChange(query)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (shouldRecreateActivity) {
            ActivityCompat.recreate(this)
            return
        }

        if (!BuildConfig.DEBUG) {
            handleAppUpdateOnResume(this)
        }
    }

    override fun recreate() {
        super.recreate()
        clearSavedLists()
        shouldRecreateActivity = false
    }

    private fun clearSavedLists() {
        audioCodecList.clear()
        videoCodecList.clear()
        drmList.clear()
    }

    override fun onDestroy() {
        clearSavedLists()
        searchListeners.clear()

        super.onDestroy()
        destroySamsungGestures()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (useImmersiveMode) {
                enableImmersiveMode()
            } else {
                disableImmersiveMode()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            decorView.systemUiVisibility = flags
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = flags
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun disableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.apply {
                show(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                systemBarsBehavior =  if (Build.VERSION.SDK_INT >= 31) {
                    WindowInsetsController.BEHAVIOR_DEFAULT
                } else {
                    WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
                }
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            decorView.systemUiVisibility = flags
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = flags
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_search -> return false

            android.R.id.home -> {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                supportFragmentManager.popBackStack()
            }

            R.id.menu_item_warning -> {
                val dialogViewBinding = DeviceIssuesLayoutBinding.inflate(layoutInflater)
                dialogViewBinding.root.adapter = DeviceIssuesAdapter(DEVICE_PROBLEMS_DB)
                val dialogBuilder = MaterialAlertDialogBuilder(this).setView(dialogViewBinding.root)
                val dialog = dialogBuilder.updateBackgroundColor(dialogBuilder.context).create()
                dialog.show()
                if (isDynamicThemingEnabled(this) && !isNativeMonetAvailable()) {
                    dialog.applyMonet()
                }
                dialog.updateButtonColors(dialogBuilder.context)
            }

            R.id.menu_item_share -> {
                val detailsFragment = supportFragmentManager.findFragmentByTag(
                        getString(R.string.details_fragment_tag)) as? DetailsFragment
                val isCodecShared = InfoType.currentInfoType != InfoType.DRM
                val codecShareOptions = if (detailsFragment != null) {
                    arrayOf(
                            getString(if (isCodecShared) R.string.codec_list else R.string.drm_list),
                            getString(R.string.codec_drm_all_info),
                            getString(if (isCodecShared) R.string.codec_details_selected else R.string.drm_details_selected))
                } else {
                    arrayOf(
                            getString(if (isCodecShared) R.string.codec_list else R.string.drm_list),
                            getString(R.string.codec_drm_all_info))
                }

                val dialogBuilder = MaterialAlertDialogBuilder(this).setTitle(R.string.choose_share)
                    .setSingleChoiceItems(codecShareOptions, -1) { dialog, option ->
                        launchShareIntent(option, detailsFragment)
                        dialog.dismiss()
                    }
                val dialog = dialogBuilder.updateBackgroundColor(dialogBuilder.context).create()
                dialog.show()
                if (isDynamicThemingEnabled(this) && !isNativeMonetAvailable()) {
                    dialog.applyMonet()
                }
                dialog.updateButtonColors(dialogBuilder.context)

                return true
            }

            R.id.menu_item_settings -> settingsContract.launch(null)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        if (binding.toolbar.background is ColorDrawable) {
            menu.updateIconColors(this, (binding.toolbar.background as ColorDrawable).color)
        } else if (binding.toolbar.background is MaterialShapeDrawable) {
            val fillColor = (binding.toolbar.background as MaterialShapeDrawable).fillColor?.defaultColor ?: getPrimaryColor(this)
            menu.updateIconColors(this, fillColor)
        }

        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.menu_item_search)
       (searchItem.actionView as SearchView).apply {
           isSubmitButtonEnabled = true
           setSearchableInfo(searchManager.getSearchableInfo(componentName))
           setOnQueryTextListener(this@MainActivity)
       }

        val knownProblems = DEVICE_PROBLEMS_DB.filter {
            it.isAffected(this, null)
        }
        if (knownProblems.isNotEmpty()) {
            menu.findItem(R.id.menu_item_warning).isVisible = true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextChange(newText: String): Boolean {
        searchListeners.forEach { it.onQueryTextChange(newText) }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchListeners.forEach { it.onQueryTextSubmit(query) }
        return true
    }

    private fun launchShareIntent(option: Int, detailsFragment: DetailsFragment?) {
        var codecId: String? = null
        var codecName: String? = null
        var drmName: String? = null
        var drmUuid: UUID? = null

        detailsFragment?.let { fragment ->
            if (fragment.isVisible) {
                codecId = fragment.codecId
                codecName = fragment.codecName
                drmName = fragment.drmName
                drmUuid = fragment.drmUuid
            }
        }
        val isCodecShared = InfoType.currentInfoType != InfoType.DRM
        if (option > 1 && ((isCodecShared && (codecId == null || codecName == null))
                || (!isCodecShared && (drmName == null || drmUuid == null)))) {
            return
        }

        val textToShare = when (option) {
            0 -> getItemListString(this)
            1 -> getAllInfoString(this)
            2 -> if (isCodecShared) {
                getSelectedCodecInfoString(this, codecId!!, codecName!!)
            } else {
                //noinspection NewApi
                getSelectedDrmInfoString(this, drmName!!, drmUuid!!)
            }
            else -> ""
        }

        val shareIntent = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)

            val title = if (option != 2) {
                getString(if (isCodecShared) R.string.codec_list else R.string.drm_list)
            } else {
                if (isCodecShared) {
                    "${getString(R.string.codec_details)}: $codecName"
                } else {
                    "${getString(R.string.drm_details)}: $drmName"
                }
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

            if (!iconFile.exists()) {
                val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_info) as VectorDrawable
                val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.run {
                    setBounds(0, 0, canvas.width, canvas.height)
                    setTint(getAttributeColor(com.google.android.material.R.attr.colorPrimary))
                    draw(canvas)
                }

                iconFile.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }

            val imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", iconFile)
            ClipData.newUri(contentResolver, null, imageUri)
        } catch (e: Exception) { null }
    }

    companion object {
        private const val INFO_ICON_FILE_NAME = "info_icon.png"
    }

}
