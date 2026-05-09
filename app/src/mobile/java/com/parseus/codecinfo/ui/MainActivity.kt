package com.parseus.codecinfo.ui

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.KeyboardShortcutInfo
import android.view.Menu
import android.view.MenuItem
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.draganddrop.DropHelper
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.kieronquinn.monetcompat.app.MonetCompatActivity
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.parseus.codecinfo.BuildConfig
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.audioCodecList
import com.parseus.codecinfo.data.codecinfo.detailedCodecInfos
import com.parseus.codecinfo.data.codecinfo.videoCodecList
import com.parseus.codecinfo.data.drm.detailedDrmInfo
import com.parseus.codecinfo.data.drm.drmList
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.databinding.ActivityMainBinding
import com.parseus.codecinfo.databinding.DeviceIssuesLayoutBinding
import com.parseus.codecinfo.ui.adapters.CodecAdapter
import com.parseus.codecinfo.ui.adapters.DeviceIssuesAdapter
import com.parseus.codecinfo.ui.adapters.DrmAdapter
import com.parseus.codecinfo.ui.externalLinks.ExternalLinksViewModel
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.ui.settings.DarkTheme
import com.parseus.codecinfo.ui.settings.SettingsContract
import com.parseus.codecinfo.utils.ExternalLinksHelper
import com.parseus.codecinfo.utils.TextSizeCache
import com.parseus.codecinfo.utils.canEnableMemoryLeakFixBackDispatcher
import com.parseus.codecinfo.utils.checkForUpdate
import com.parseus.codecinfo.utils.cleanInAppUpdateReferences
import com.parseus.codecinfo.utils.copyToClipboard
import com.parseus.codecinfo.utils.createInAppUpdateResultLauncher
import com.parseus.codecinfo.utils.disableApiBlacklistOnPie
import com.parseus.codecinfo.utils.getAllInfoString
import com.parseus.codecinfo.utils.getItemListString
import com.parseus.codecinfo.utils.getMemoryLeakFixBackDispatcher
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.getSelectedCodecInfoString
import com.parseus.codecinfo.utils.getSelectedDrmInfoString
import com.parseus.codecinfo.utils.getSurfaceColor
import com.parseus.codecinfo.utils.handleAppUpdateOnResume
import com.parseus.codecinfo.utils.initializeAppRating
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isInTwoPaneMode
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.updateBackgroundColor
import com.parseus.codecinfo.utils.updateButtonColors
import com.parseus.codecinfo.utils.updateColors
import com.parseus.codecinfo.utils.updateIconColors
import com.parseus.codecinfo.utils.updateNavigationBarColor
import com.parseus.codecinfo.utils.updateStatusBarColor
import com.parseus.codecinfo.utils.updateToolBarColor
import com.parseus.codecinfo.viewmodels.ItemsViewModel
import com.parseus.codecinfo.viewmodels.SearchViewModel
import dev.kdrag0n.monet.theme.ColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class MainActivity : MonetCompatActivity() {

    internal lateinit var binding: ActivityMainBinding

    private val externalLinksViewModel: ExternalLinksViewModel by viewModels()
    private lateinit var externalLinksHelper: ExternalLinksHelper

    private val useImmersiveMode: Boolean
        get() = settingsRepository.getSettingsSync().immersiveMode

    private val settingsContract = registerForActivityResult(SettingsContract()) { result ->
        if (result.dynamicThemeChanged || result.immersiveChanged) {
            ActivityCompat.recreate(this)
        } else {
            if (result.shouldReloadLists() || result.saveDetailsToLogcatChanged) {
                itemsViewModel.refreshData(this)
            }
        }
    }

    private val voiceSearchContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val queries = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!queries.isNullOrEmpty()) {
                val query = queries[0]
                searchViewModel.setSearchQuery(query)
                binding.searchView.setText(query)
                binding.searchView.show()
            }
        }
    }

    private val searchViewModel: SearchViewModel by viewModels()
    private val itemsViewModel: ItemsViewModel by viewModels()

    override val recreateMode: Boolean
        get() = !isNativeMonetAvailable()
    override val updateOnCreate: Boolean
        get() = !isNativeMonetAvailable()

    private val memoryLeakFixBackDispatcher = getMemoryLeakFixBackDispatcher()
    private val homeAsUpBackDispatcher = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (binding.searchView.isShowing) {
                hideSearchView()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    init {
        createInAppUpdateResultLauncher(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CodecInfo)

        val surfaceColor = getSurfaceColor(this)
        window.setBackgroundDrawable(surfaceColor.toDrawable())

        var isUiReady = false
        installSplashScreen().setKeepOnScreenCondition { !isUiReady }

        disableApiBlacklistOnPie()

        window.apply {
            reenterTransition = null
            exitTransition = null
            allowEnterTransitionOverlap = true
            allowReturnTransitionOverlap = true
        }

        WindowCompat.enableEdgeToEdge(window)

        super.onCreate(savedInstanceState)

        externalLinksHelper = ExternalLinksHelper(this, lifecycle)
        externalLinksViewModel.prefetchExternalLink.observe(this) {
            if (it != null) {
                externalLinksHelper.prefetchUrl(it)
            }
        }

        if (!isNativeMonetAvailable()) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    monet.awaitMonetReady()
                    initializeUI(savedInstanceState)
                    isUiReady = true
                    window.updateStatusBarColor(this@MainActivity)
                    window.updateNavigationBarColor(this@MainActivity)
                }
            }
        } else {
            monet.removeMonetColorsChangedListener(this)
            initializeUI(savedInstanceState)
            isUiReady = true
            window.updateStatusBarColor(this)
            window.updateNavigationBarColor(this)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateUIState()
        }
        onBackPressedDispatcher.addCallback(memoryLeakFixBackDispatcher)
        onBackPressedDispatcher.addCallback(homeAsUpBackDispatcher)
    }

    override fun onKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_F -> {
                binding.searchView.show()
                return true
            }
            KeyEvent.KEYCODE_S -> {
                onOptionsItemSelected(binding.searchBar.menu.findItem(R.id.menu_item_share))
                return true
            }
            KeyEvent.KEYCODE_COMMA -> {
                settingsContract.launch(null)
                return true
            }
            KeyEvent.KEYCODE_C -> {
                copyCurrentSelection()
                return true
            }
            KeyEvent.KEYCODE_V -> {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                handleDroppedText(clipboard.primaryClip?.getItemAt(0)?.text?.toString())
                return true
            }
        }
        return super.onKeyShortcut(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ESCAPE && event.repeatCount == 0) {
            if (binding.searchView.isShowing) {
                hideSearchView()
                return true
            } else if (supportFragmentManager.backStackEntryCount > 0) {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @RequiresApi(24)
    override fun onProvideKeyboardShortcuts(
        data: MutableList<KeyboardShortcutGroup>,
        menu: Menu?,
        deviceId: Int
    ) {
        val group = KeyboardShortcutGroup(getString(R.string.keyboard_shortcuts)).apply {
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_search), KeyEvent.KEYCODE_F, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_share), KeyEvent.KEYCODE_S, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_settings), KeyEvent.KEYCODE_COMMA, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_copy), KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_paste), KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_close), KeyEvent.KEYCODE_ESCAPE, 0))
        }
        data.add(group)
    }

    fun showKeyboardShortcutsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.keyboard_shortcuts_dialog, null)
        val dialogBuilder = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.keyboard_shortcuts)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
        val dialog = dialogBuilder.updateBackgroundColor(this).create()
        dialog.show()
        if (isDynamicThemingEnabled(this) && !isNativeMonetAvailable()) {
            dialog.applyMonet()
        }
        dialog.updateButtonColors(this)
    }

    private fun copyCurrentSelection() {
        val focusedView = window.currentFocus
        val textToCopy = focusedView?.tag?.toString()

        if (textToCopy != null) {
            copyToClipboard(getString(R.string.app_name), textToCopy, binding.root)
        } else {
            val detailsFragment = supportFragmentManager
                .findFragmentByTag(getString(R.string.details_fragment_tag)) as? DetailsFragment
            if (detailsFragment != null && detailsFragment.isVisible) {
                val name = detailsFragment.codecName ?: detailsFragment.drmName
                if (name != null) {
                    copyToClipboard(getString(R.string.app_name), name, binding.root)
                }
            }
        }
    }

    fun updateUIState(forceHideDetails: Boolean = false) {
        if (!::binding.isInitialized) return

        val detailsFragment = supportFragmentManager.findFragmentByTag(getString(R.string.details_fragment_tag))
        val isDetailsShown = !forceHideDetails && supportFragmentManager.backStackEntryCount > 0 && detailsFragment?.isRemoving != true
        val isTwoPane = isInTwoPaneMode()

        binding.toolbar.updateToolBarColor(this)

        if (isTwoPane) {
            binding.toolbar.isVisible = false
            binding.searchBar.isVisible = true
            binding.searchBar.apply {
                setNavigationIcon(R.drawable.ic_search)
                setNavigationContentDescription(R.string.action_search)
                setNavigationOnClickListener(null)
            }
        } else {
            binding.toolbar.isVisible = isDetailsShown
            binding.searchBar.isVisible = !isDetailsShown

            if (isDetailsShown) {
                binding.toolbar.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            } else {
                binding.searchBar.apply {
                    setNavigationIcon(R.drawable.ic_search)
                    setNavigationContentDescription(R.string.action_search)
                    setNavigationOnClickListener(null)
                }
            }
        }
        binding.searchBar.setHint(R.string.search_hint)

        memoryLeakFixBackDispatcher.isEnabled = canEnableMemoryLeakFixBackDispatcher()
        homeAsUpBackDispatcher.isEnabled = binding.searchView.isShowing
    }

    private fun initializeUI(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT >= 24) {
            val handIcon = PointerIcon.getSystemIcon(this, PointerIcon.TYPE_HAND)
            // Target only interactive buttons in the bars
            binding.toolbar.forEach { if (it is ImageButton) it.pointerIcon = handIcon }
            binding.searchBar.forEach { if (it is ImageButton) it.pointerIcon = handIcon }
        }

        setupFoldableSupport()

        setContentView(binding.root)

        if (useImmersiveMode) {
            enableImmersiveMode()
        } else {
            disableImmersiveMode()
        }

        val settings = settingsRepository.getSettingsSync()
        val darkTheme = settings.darkTheme
        AppCompatDelegate.setDefaultNightMode(DarkTheme.getAppCompatValue(darkTheme))

        binding.searchBar.setHint(R.string.search_hint)
        binding.searchBar.inflateMenu(R.menu.app_bar_menu)
        binding.searchBar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }
        updateUIState()

        binding.updateProgressBar.updateColors(this)

        itemsViewModel.loadData(this)

        lifecycleScope.launch {
            val affectedProblems = withContext(Dispatchers.IO) {
                DEVICE_PROBLEMS_DB.filter { it.isAffected(this@MainActivity, null) }
            }
            affectedProblems.forEach { problem ->
                problem.urls.forEach { url ->
                    externalLinksViewModel.prefetchExternalLink.value = url.toUri()
                }
            }
        }

        setupSearch()

        binding.appBar.updateBackgroundColor(this)
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { v, windowInsets ->
            val insets = windowInsets.getInsets(systemBars() or displayCutout())

            // Apply system bar height as top padding (and left/right respectively)
            v.updatePadding(left = insets.left, top = insets.top, right = insets.right)
            windowInsets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchView) { v, windowInsets ->
            val insets = windowInsets.getInsets(systemBars() or displayCutout())
            v.updatePadding(left = insets.left, top = insets.top, right = insets.right, bottom = insets.bottom)
            windowInsets
        }
        ViewGroupCompat.installCompatInsetsDispatch(binding.root)
        binding.root.requestApplyInsets()
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentFragment) { view, windowInsets ->
            var consumed = false
            (view as ViewGroup).forEach { child ->
                val childResult = ViewCompat.dispatchApplyWindowInsets(child, windowInsets)
                if (childResult.isConsumed) {
                    consumed = true
                }
            }

            if (consumed) WindowInsetsCompat.CONSUMED else windowInsets
        }

        if (savedInstanceState != null) {
            recreateDetailFragmentIfNeedded()
        }

        handleIntent(intent)

        @Suppress("KotlinConstantConditions")
        if (!BuildConfig.DEBUG) {
            initializeAppRating(this)
            checkForUpdate(this, binding.updateProgressBar)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun setupSearch() {
        binding.searchView.setupWithSearchBar(binding.searchBar)
        binding.searchView.apply {
            editText.setOnEditorActionListener { _, _, _ ->
                val query = text.toString()
                searchViewModel.setSearchQuery(query)
                editText.clearFocus()
                false
            }
            editText.addTextChangedListener {
                searchViewModel.setSearchQuery(it.toString())
            }
            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if ((binding.searchResultsRecyclerView.adapter?.itemCount ?: 0) > 0) {
                        binding.searchResultsRecyclerView.requestFocus()
                        return@setOnKeyListener true
                    }
                }
                false
            }

            val dropMimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN, ClipDescription.MIMETYPE_TEXT_HTML)
            DropHelper.configureView(this@MainActivity, binding.searchBar, dropMimeTypes) { _, payload ->
                handleDroppedText(payload.clip.getItemAt(0).text?.toString())
                null
            }
            DropHelper.configureView(this@MainActivity, editText, dropMimeTypes) { _, payload ->
                handleDroppedText(payload.clip.getItemAt(0).text?.toString())
                null
            }

            addTransitionListener { _, _, newState ->
                when (newState) {
                    SearchView.TransitionState.SHOWING -> {
                        binding.appBar.isInvisible = true
                        homeAsUpBackDispatcher.isEnabled = true
                    }
                    SearchView.TransitionState.HIDING -> {
                        binding.appBar.isVisible = true
                        updateUIState()
                    }
                    SearchView.TransitionState.HIDDEN -> {
                        binding.appBar.isVisible = true
                        setText("")
                        searchViewModel.setSearchQuery("")
                        updateUIState()
                    }
                    else -> {}
                }
            }
        }

        val audioSearchAdapter = CodecAdapter()
        val videoSearchAdapter = CodecAdapter()
        val drmSearchAdapter = DrmAdapter()
        val concatAdapter = ConcatAdapter(audioSearchAdapter, videoSearchAdapter, drmSearchAdapter)

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = concatAdapter
            addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
        }

        searchViewModel.initData(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchResultState.collect { state ->
                    audioSearchAdapter.updateSearchQuery(state.query)
                    videoSearchAdapter.updateSearchQuery(state.query)
                    drmSearchAdapter.updateSearchQuery(state.query)

                    if (state.isQueryEmpty) {
                        audioSearchAdapter.submitList(emptyList())
                        videoSearchAdapter.submitList(emptyList())
                        drmSearchAdapter.submitList(emptyList())
                    } else {
                        audioSearchAdapter.submitList(state.audioResults)
                        videoSearchAdapter.submitList(state.videoResults)
                        drmSearchAdapter.submitList(state.drmResults)
                    }
                }
            }
        }
    }

    fun hideSearchView() {
        binding.searchView.setText("")
        if (binding.searchView.isShowing) {
            binding.searchView.hide()
        }
    }

    private fun handleDroppedText(text: String?) {
        if (text != null) {
            val cleanedText = text.replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "").trim()
            if (cleanedText.isNotEmpty()) {
                if (!binding.searchView.isShowing) {
                    binding.searchView.show()
                }
                binding.searchView.setText(cleanedText)
                searchViewModel.setSearchQuery(cleanedText)
            }
        }
    }

    private fun setupFoldableSupport() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .collect { newLayoutInfo ->
                        if (binding.verticalSeparatorGuideline == null) return@collect

                        val foldingFeature = newLayoutInfo.displayFeatures
                            .filterIsInstance<FoldingFeature>()
                            .firstOrNull()

                        val constraintSet = ConstraintSet()
                        constraintSet.clone(binding.root)

                        if (foldingFeature != null) {
                            val isVertical = foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL
                            val location = IntArray(2)
                            binding.root.getLocationInWindow(location)

                            if (isVertical) {
                                val xOffset = location[0]
                                val guidePosition = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                                    val windowWidth = WindowMetricsCalculator.getOrCreate()
                                        .computeCurrentWindowMetrics(this@MainActivity).bounds.width()
                                    windowWidth - (foldingFeature.bounds.right - xOffset)
                                } else {
                                    foldingFeature.bounds.left - xOffset
                                }

                                // Update Vertical Guideline position
                                constraintSet.setGuidelineBegin(R.id.verticalSeparatorGuideline, guidePosition)
                                constraintSet.setGuidelinePercent(R.id.horizontalSeparatorGuideline, -1f)

                                // Re-apply side-by-side constraints
                                constraintSet.connect(R.id.content_fragment, ConstraintSet.END, R.id.verticalSeparatorGuideline, ConstraintSet.START)
                                constraintSet.connect(R.id.content_fragment, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.START, R.id.verticalSeparatorGuideline, ConstraintSet.END)
                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.TOP, R.id.appBar, ConstraintSet.BOTTOM)
                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                                // Apply safety margins/hinge width
                                val hingeWidth = foldingFeature.bounds.width()
                                val safetyPadding = if (foldingFeature.isSeparating) (16 * resources.displayMetrics.density).toInt() else 0
                                constraintSet.setMargin(R.id.itemDetailsFragment, ConstraintSet.START, hingeWidth + safetyPadding)
                                constraintSet.setMargin(R.id.content_fragment, ConstraintSet.END, safetyPadding)

                                // Reset horizontal margins
                                constraintSet.setMargin(R.id.itemDetailsFragment, ConstraintSet.TOP, 0)
                                constraintSet.setMargin(R.id.content_fragment, ConstraintSet.BOTTOM, 0)

                                // Hide a manual divider on devices with a physical hinge
                                // (e.g. Surface Duo).
                                constraintSet.setVisibility(R.id.separator,
                                    if (foldingFeature.isSeparating) View.GONE else View.VISIBLE)
                            } else {
                                val yOffset = location[1]
                                val guidePosition = foldingFeature.bounds.top - yOffset

                                // Update Horizontal Guideline position
                                constraintSet.setGuidelineBegin(R.id.horizontalSeparatorGuideline, guidePosition)
                                constraintSet.setGuidelinePercent(R.id.verticalSeparatorGuideline, -1f)

                                // Stack vertically: List on top, Details on bottom
                                constraintSet.connect(R.id.content_fragment, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                                constraintSet.connect(R.id.content_fragment, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                                constraintSet.connect(R.id.content_fragment, ConstraintSet.BOTTOM, R.id.horizontalSeparatorGuideline, ConstraintSet.TOP)

                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.TOP, R.id.horizontalSeparatorGuideline, ConstraintSet.BOTTOM)
                                constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                                // Apply safety margins for the horizontal crease
                                val safetyPadding = if (foldingFeature.isSeparating) (16 * resources.displayMetrics.density).toInt() else 0
                                constraintSet.setMargin(R.id.itemDetailsFragment, ConstraintSet.TOP, foldingFeature.bounds.height() + safetyPadding)
                                constraintSet.setMargin(R.id.content_fragment, ConstraintSet.BOTTOM, safetyPadding)

                                // Reset vertical margins
                                constraintSet.setMargin(R.id.itemDetailsFragment, ConstraintSet.START, 0)
                                constraintSet.setMargin(R.id.content_fragment, ConstraintSet.END, 0)

                                // ALWAYS hide the vertical separator in tabletop mode
                                constraintSet.setVisibility(R.id.separator,View.GONE)
                            }
                        } else {
                            // Reset to default side-by-side tablet mode
                            val defaultPercent = ResourcesCompat.getFloat(resources, R.dimen.separator_guideline_percent)
                            constraintSet.setGuidelinePercent(R.id.verticalSeparatorGuideline, defaultPercent)
                            constraintSet.setGuidelinePercent(R.id.horizontalSeparatorGuideline, -1f)

                            constraintSet.connect(R.id.content_fragment, ConstraintSet.END, R.id.verticalSeparatorGuideline, ConstraintSet.START)
                            constraintSet.connect(R.id.content_fragment, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                            constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.START, R.id.verticalSeparatorGuideline, ConstraintSet.END)
                            constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.TOP, R.id.appBar, ConstraintSet.BOTTOM)
                            constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                            constraintSet.connect(R.id.itemDetailsFragment, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                            constraintSet.setMargin(R.id.itemDetailsFragment, ConstraintSet.START, 0)
                            constraintSet.setMargin(R.id.itemDetailsFragment, ConstraintSet.TOP, 0)
                            constraintSet.setMargin(R.id.content_fragment, ConstraintSet.END, 0)
                            constraintSet.setMargin(R.id.content_fragment, ConstraintSet.BOTTOM, 0)

                            constraintSet.setVisibility(R.id.separator, View.VISIBLE)
                        }

                        constraintSet.applyTo(binding.root)
                    }
            }
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
                    updateUIState()
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
                searchViewModel.setSearchQuery(query)
                binding.searchView.setText(query)
                binding.searchView.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        @Suppress("KotlinConstantConditions")
        if (!BuildConfig.DEBUG) {
            handleAppUpdateOnResume(this)
        }
    }

    override fun recreate() {
        super.recreate()
        clearSavedLists()
    }

    private fun clearSavedLists() {
        audioCodecList.clear()
        videoCodecList.clear()
        drmList.clear()
        detailedCodecInfos.clear()
        detailedDrmInfo.clear()
        TextSizeCache.clear()
    }

    @Suppress("USELESS_CAST")
    override fun onDestroy() {
        window.exitTransition = null
        window.reenterTransition = null

        setExitSharedElementCallback(null as? SharedElementCallback)
        setEnterSharedElementCallback(null as? SharedElementCallback)

        clearSavedLists()
        cleanInAppUpdateReferences()
        super.onDestroy()
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

    private fun enableImmersiveMode() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE    }

    private fun disableImmersiveMode() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.show(systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        binding.root.post {
            binding.root.requestApplyInsets()
        }
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.searchBar.navigationIcon = null
                onBackPressedDispatcher.onBackPressed()
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

            R.id.menu_item_voice_search -> {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.search_hint))
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                try {
                    voiceSearchContract.launch(intent)
                } catch (_: ActivityNotFoundException) {
                    Snackbar.make(
                        this,
                        findViewById(android.R.id.content),
                        getString(R.string.no_apps_for_action),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
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

            R.id.menu_item_keyboard_shortcuts -> {
                showKeyboardShortcutsDialog()
                return true
            }

            R.id.menu_item_settings -> {
                val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                settingsContract.launch(null, activityOptions)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menu = binding.searchBar.menu
        if (binding.searchBar.background is ColorDrawable) {
            menu.updateIconColors(this, (binding.searchBar.background as ColorDrawable).color)
        } else if (binding.searchBar.background is MaterialShapeDrawable) {
            val fillColor = (binding.searchBar.background as MaterialShapeDrawable).fillColor?.defaultColor ?: getPrimaryColor(this)
            menu.updateIconColors(this, fillColor)
        }

        val affectedByKnownProblems = DEVICE_PROBLEMS_DB.any {
            it.isAffected(this, null)
        }
        if (affectedByKnownProblems) {
            menu.findItem(R.id.menu_item_warning).isVisible = true
        }

        return super.onCreateOptionsMenu(menu)
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

        lifecycleScope.launch {
            val textToShare = withContext(Dispatchers.IO) {
                when (option) {
                    0 -> getItemListString(this@MainActivity)
                    1 -> getAllInfoString(this@MainActivity)
                    2 -> if (isCodecShared) {
                        getSelectedCodecInfoString(this@MainActivity, codecId!!, codecName!!)
                    } else {
                        //noinspection NewApi
                        getSelectedDrmInfoString(this@MainActivity, drmName!!, drmUuid!!)
                    }
                    else -> ""
                }
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
    }

    @RequiresApi(29)
    private suspend fun storeInfoIconForShare(): ClipData? = withContext(Dispatchers.IO) {
        try {
            val iconColor = getPrimaryColor(this@MainActivity)
            // Include color in filename to handle theme/dynamic color changes
            val fileName = "$INFO_ICON_FILE_NAME_PREFIX${Integer.toHexString(iconColor)}.png"
            val iconFile = File(filesDir, fileName)

            if (!iconFile.exists()) {
                val drawable = AppCompatResources.getDrawable(this@MainActivity,
                    R.drawable.ic_info)?.mutate() ?: return@withContext null
                val bitmap = drawable.toBitmap(
                    width = drawable.intrinsicWidth,
                    height = drawable.intrinsicHeight,
                    config = Bitmap.Config.ARGB_8888
                ).apply {
                    val canvas = Canvas(this)
                    drawable.mutate().apply {
                        setBounds(0, 0, canvas.width, canvas.height)
                        setTint(iconColor)
                        draw(canvas)
                    }
                }

                iconFile.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }

            val imageUri = FileProvider.getUriForFile(this@MainActivity, "${packageName}.fileprovider", iconFile)
            ClipData.newUri(contentResolver, null, imageUri)
        } catch (_: Exception) { null }
    }

    fun shareSingleItem(textToShare: String, title: String) {
        lifecycleScope.launch {
            val shareIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textToShare)
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
    }

    companion object {
        private const val INFO_ICON_FILE_NAME_PREFIX = "info_icon_"
    }

}
