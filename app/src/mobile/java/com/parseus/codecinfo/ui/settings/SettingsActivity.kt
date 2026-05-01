package com.parseus.codecinfo.ui.settings

import android.annotation.SuppressLint
import android.app.assist.AssistContent
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.kieronquinn.monetcompat.app.MonetCompatActivity
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.databinding.SettingsMainBinding
import com.parseus.codecinfo.databinding.WallpaperColorPickerLayoutBinding
import com.parseus.codecinfo.ui.CustomLinearLayoutManager
import com.parseus.codecinfo.ui.fragments.AboutFragment
import com.parseus.codecinfo.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import com.google.android.material.motion.MotionUtils
import com.parseus.codecinfo.ui.externalLinks.ExternalLinksViewModel
import kotlin.getValue

class SettingsActivity : MonetCompatActivity() {

    private lateinit var binding: SettingsMainBinding

    override val recreateMode: Boolean
        get() = !isNativeMonetAvailable()
    override val updateOnCreate: Boolean
        get() = !isNativeMonetAvailable()

    private val externalLinksViewModel: ExternalLinksViewModel by viewModels()
    private lateinit var externalLinksHelper: ExternalLinksHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val startingFromAlias = intent?.component?.className?.startsWith("alias.SettingsActivity") == true
        if (startingFromAlias) {
            delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        setTheme(R.style.Theme_CodecInfo_Settings)
        postponeEnterTransition()
        super.onCreate(savedInstanceState)

        WindowCompat.enableEdgeToEdge(window)

        externalLinksHelper = ExternalLinksHelper(this, lifecycle)
        externalLinksViewModel.launchExternalLink.observe(this) {
            if (it != null) {
                externalLinksHelper.launchInBrowser(this, it)
            }
        }

        val emphasizedInterpolator = MotionUtils.resolveThemeInterpolator(
            this,
            com.google.android.material.R.attr.motionEasingEmphasizedInterpolator,
            AnimationUtils.loadInterpolator(this, androidx.appcompat.R.interpolator.fast_out_slow_in)
        )
        val enter = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration = 500
            interpolator = emphasizedInterpolator
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
        }
        val returnTrans = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration = 500
            interpolator = emphasizedInterpolator
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
        }
        window.apply {
            enterTransition = enter
            returnTransition = returnTrans
            allowEnterTransitionOverlap = true
            allowReturnTransitionOverlap = true
        }

        if (!isNativeMonetAvailable()) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    monet.awaitMonetReady()
                    initializeUI(savedInstanceState)
                    binding.root.setBackgroundColor(getSurfaceColor(this@SettingsActivity))
                    startPostponedEnterTransition()
                }
            }
        } else {
            initializeUI(savedInstanceState)
            startPostponedEnterTransition()
        }

        onBackPressedDispatcher.addCallback(this) {
            if (supportFragmentManager.findFragmentByTag("about_fragment") != null) {
                goBackToMainFragment()
            } else {
                // Needed both for a memory leak fix on Android 10 and to ensure proper transitions.
                finishAfterTransition()
            }
        }
    }

    private fun initializeUI(savedInstanceState: Bundle?) {
        binding = SettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(!isChromebook(this))
        if (savedInstanceState == null) {
            supportFragmentManager.commit { replace(R.id.content, SettingsFragment::class.java, null) }
        }
        window.updateStatusBarColor(this)
        binding.toolbar.updateToolBarColor(this)
        binding.appBar.updateBackgroundColor(this)
        ViewGroupCompat.installCompatInsetsDispatch(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun goBackToMainFragment() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(!isChromebook(this))
        supportActionBar!!.title = getString(R.string.action_settings)
        supportFragmentManager.popBackStack()
    }

    @Suppress("USELESS_CAST")
    override fun onDestroy() {
        window.exitTransition = null
        window.reenterTransition = null

        setExitSharedElementCallback(null as? SharedElementCallback)
        setEnterSharedElementCallback(null as? SharedElementCallback)

        super.onDestroy()
    }

    override fun finish() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ALIASES_CHANGED, aliasesChanged)
            putExtra(FILTER_TYPE_CHANGED, filterTypeChanged)
            putExtra(SORTING_CHANGED, sortingChanged)
            putExtra(IMMERSIVE_CHANGED, immersiveChanged)
            putExtra(DYNAMIC_THEME_CHANGED, dynamicThemeChanged)
            putExtra(HW_ICON_CHANGED, hwIconChanged)
            putExtra(SAVE_DETAILS_TO_LOGCAT_CHANGED, saveDetailsToLogcatChanged)
            putExtra(HW_ONLY_CODECS_CHANGED, hwOnlyCodecsChanged)
        })
        super.finish()

        aliasesChanged = false
        filterTypeChanged = false
        sortingChanged = false
        immersiveChanged = false
        dynamicThemeChanged = false
        hwIconChanged = false
        saveDetailsToLogcatChanged = false
        hwOnlyCodecsChanged = false
    }

    override fun onProvideAssistContent(outContent: AssistContent) {
        super.onProvideAssistContent(outContent)

        if (Build.VERSION.SDK_INT >= 31) {
            outContent.webUri = externalLinksViewModel.urlOpened
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val view = super.onCreateView(inflater, container, savedInstanceState)

            findPreference<CheckBoxPreference>("dynamic_theme")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    if (!isNativeMonetAvailable()) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                val wallpaperColors = MonetCompat.getInstance().getAvailableWallpaperColors() ?: emptyList()
                                findPreference<Preference>("show_wallaper_colors")?.isVisible =
                                    newValue as Boolean && wallpaperColors.size > 1
                                            && (findPreference<CheckBoxPreference>("dynamic_theme")?.isChecked ?: false)
                            }
                        }
                    }

                    dynamicThemeChanged = true
                    activity?.let { ActivityCompat.recreate(it) }
                    true
                }
            }

            if (!isNativeMonetAvailable()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                        findPreference<Preference>("show_wallpaper_colors")?.apply {
                            val wallpaperColors = MonetCompat.getInstance().getAvailableWallpaperColors() ?: emptyList()
                            isVisible = wallpaperColors.size > 1 && (findPreference<CheckBoxPreference>("dynamic_theme")?.isChecked ?: false)
                        }
                    }
                }

                findPreference<ListPreference>("dynamic_theme_wallpaper_source")?.apply {
                    isVisible = Build.VERSION.SDK_INT >= 27
                            && (findPreference<CheckBoxPreference>("dynamic_theme")?.isChecked ?: false)
                    setOnPreferenceChangeListener { _, newValue ->
                        MonetCompat.wallpaperSource = (newValue as String).toInt()
                        MonetCompat.getInstance().updateMonetColors()
                        true
                    }
                }
            }

            findPreference<CheckBoxPreference>("immersive_mode")?.apply {
                setOnPreferenceChangeListener { _, _ ->
                    immersiveChanged = true
                    true
                }
            }

            findPreference<CheckBoxPreference>("show_aliases")?.apply {
                if (Build.VERSION.SDK_INT >= 29) {
                    setOnPreferenceChangeListener { _, _ ->
                        aliasesChanged = true
                        true
                    }
                } else {
                    isVisible = false
                }
            }

            findPreference<ListPreference>("dark_theme")!!.apply {
                setDarkThemeOptions(this)
                val currentTheme = value ?: getDefaultThemeOption(requireContext()).toString()
                icon = AppCompatResources.getDrawable(requireContext(),
                        getCurrentThemeIcon(currentTheme.toInt()))
                setOnPreferenceChangeListener { pref, newValue ->
                    (newValue as String)
                    pref.icon = AppCompatResources.getDrawable(requireContext(),
                            getCurrentThemeIcon(newValue.toInt()))
                    AppCompatDelegate.setDefaultNightMode(DarkTheme.getAppCompatValue(newValue.toInt()))
                    true
                }
            }

            val filterType = findPreference<ListPreference>("filter_type")
            filterType!!.setOnPreferenceChangeListener { _, _ ->
                filterTypeChanged = true
                true
            }

            val sortingType = findPreference<ListPreference>("sort_type")
            sortingType!!.setOnPreferenceChangeListener { _, _ ->
                sortingChanged = true
                true
            }

            val showHwIcon = findPreference<CheckBoxPreference>("show_hw_icon")
            showHwIcon!!.setOnPreferenceChangeListener { _, _ ->
                hwIconChanged = true
                true
            }

            val saveDetailsToLogcat = findPreference<CheckBoxPreference>("save_details_to_logcat")
            saveDetailsToLogcat!!.setOnPreferenceChangeListener { _, _ ->
                saveDetailsToLogcatChanged = true
                true
            }

            val hwOnlyCodecs = findPreference<CheckBoxPreference>("show_hw_codecs_only")
            hwOnlyCodecs!!.setOnPreferenceChangeListener { _, _ ->
                hwOnlyCodecsChanged = true
                true
            }

            return view
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
                view.applyMonetRecursively()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = requireContext().settingsRepository
            addPreferencesFromResource(R.xml.preferences_screen)
        }

        @SuppressLint("InflateParams", "NewApi", "ApplySharedPref")
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            return when (preference.key) {
                "show_wallpaper_colors" -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            val availableColors = MonetCompat.getInstance().getAvailableWallpaperColors() ?: emptyList()
                            if (availableColors.isNotEmpty()) {
                                showWallpaperColorPicker(availableColors)
                            } else {
                                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                    R.string.dynamic_theme_color_picker_unavailable, Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                    true
                }

                "feedback" -> {
                    sendFeedbackEmail()
                    true
                }

                "keyboard_shortcuts" -> {
                    showKeyboardShortcutsDialog()
                    true
                }

                "help" -> {
                    if (activity != null) {
                        parentFragmentManager.commit {
                            replace(R.id.content, AboutFragment(), "about_fragment")
                            addToBackStack(null)
                            (requireActivity() as AppCompatActivity).supportActionBar!!.apply {
                                title = getString(R.string.about_app)
                                setDisplayHomeAsUpEnabled(true)
                            }
                        }
                    }
                    true
                }

                else -> super.onPreferenceTreeClick(preference)
            }
        }

        @SuppressLint("NewApi")
        private suspend fun showWallpaperColorPicker(availableColors: List<Int>) {
            val dialogBuilder =  MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dynamic_theme_wallpaper_color_picker)
            val alertDialog = dialogBuilder.updateBackgroundColor(dialogBuilder.context).create()
            val colorPickerView = WallpaperColorPickerLayoutBinding.inflate(layoutInflater).root
            colorPickerView.apply {
                setBackgroundTintList(ColorStateList.valueOf(
                    MonetCompat.getInstance().getBackgroundColor(requireContext())
                ))
                layoutManager =
                    CustomLinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

                adapter = ColorPickerAdapter(
                    requireContext(),
                    MonetCompat.getInstance().getSelectedWallpaperColor(),
                    availableColors
                ) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            withContext(Dispatchers.IO) {
                                requireContext().settingsRepository.setSelectedColor(it)
                            }
                            alertDialog.dismiss()
                            MonetCompat.getInstance().updateMonetColors()
                        }
                    }
                }
            }
            alertDialog.setView(colorPickerView)
            alertDialog.show()
            alertDialog.applyMonet()
        }

        private fun getCurrentThemeIcon(type: Int) = when (type) {
            DarkTheme.SystemDefault.value -> R.drawable.ic_app_theme_system
            DarkTheme.BatterySaver.value -> R.drawable.ic_app_theme_battery
            DarkTheme.Dark.value -> R.drawable.ic_app_theme_dark
            else -> R.drawable.ic_app_theme_light
        }

        private fun setDarkThemeOptions(listPreference: ListPreference) {
            val entries = mutableListOf<CharSequence>()
            val entryValues = mutableListOf<CharSequence>()

            // Light and Dark (always present)
            entries.add(getString(R.string.app_theme_light))
            entries.add(getString(R.string.app_theme_dark))
            entryValues.add(DarkTheme.Light.value.toString())
            entryValues.add(DarkTheme.Dark.value.toString())

            // Set by battery saver (if not blacklisted)
            if (!isBatterySaverDisallowed(requireContext())) {
                entries.add(getString(R.string.app_theme_battery_saver))
                entryValues.add(DarkTheme.BatterySaver.value.toString())
            }

            // System default (Android 9.0+)
            if (Build.VERSION.SDK_INT >= 28) {
                entries.add(getString(R.string.app_theme_system_default))
                entryValues.add(DarkTheme.SystemDefault.value.toString())
            }

            listPreference.apply {
                this.entries = entries.toTypedArray()
                this.entryValues = entryValues.toTypedArray()
                setDefaultValue(getDefaultThemeOption(requireContext()).toString())
            }
        }

        private fun showKeyboardShortcutsDialog() {
            val dialogView = layoutInflater.inflate(R.layout.keyboard_shortcuts_dialog, null)
            val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.keyboard_shortcuts)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
            val dialog = dialogBuilder.updateBackgroundColor(requireContext()).create()
            dialog.show()
            if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
                dialog.applyMonet()
            }
            dialog.updateButtonColors(requireContext())
        }

    }

    companion object {
        var aliasesChanged = false
        var filterTypeChanged = false
        var sortingChanged = false
        var immersiveChanged = false
        var dynamicThemeChanged = false
        var hwIconChanged = false
        var saveDetailsToLogcatChanged = false
        var hwOnlyCodecsChanged = false
        const val ALIASES_CHANGED = "aliases_changed"
        const val FILTER_TYPE_CHANGED = "filter_type_changed"
        const val SORTING_CHANGED = "sorting_changed"
        const val IMMERSIVE_CHANGED = "immersive_changed"
        const val DYNAMIC_THEME_CHANGED = "dynamic_theme_changed"
        const val HW_ICON_CHANGED = "hw_icon_changed"
        const val SAVE_DETAILS_TO_LOGCAT_CHANGED = "save_details_to_logcat_changed"
        const val HW_ONLY_CODECS_CHANGED = "hw_only_codecs_changed"
    }

}