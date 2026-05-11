package com.parseus.codecinfo.ui

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_STEM_1
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.tracing.trace
import androidx.viewpager2.widget.ViewPager2
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.widget.SwipeDismissFrameLayout
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.audioCodecList
import com.parseus.codecinfo.data.codecinfo.detailedCodecInfos
import com.parseus.codecinfo.data.codecinfo.videoCodecList
import com.parseus.codecinfo.data.drm.detailedDrmInfo
import com.parseus.codecinfo.data.drm.drmList
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.databinding.WearActivityMainBinding
import com.parseus.codecinfo.ui.WearSearchActivity.Companion.EXTRA_QUERY
import com.parseus.codecinfo.ui.adapters.WearPagerAdapter
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.ui.fragments.WearItemFragment
import com.parseus.codecinfo.utils.TextSizeCache
import com.parseus.codecinfo.viewmodels.ItemsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WearMainActivity : AppCompatActivity() {

    private lateinit var binding: WearActivityMainBinding
    private val viewModel: ItemsViewModel by viewModels()

    private val remoteInputLanucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val results = RemoteInput.getResultsFromIntent(it.data)
            val query = results?.getCharSequence(EXTRA_QUERY)?.toString()
            if (!query.isNullOrBlank()) {
                val intent = Intent(this, WearSearchActivity::class.java).apply {
                    putExtra("query", query)
                }
                startActivity(intent)
            }
        }
    }

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            viewModel.setAmbientMode(true)
        }

        override fun onExitAmbient() {
            viewModel.setAmbientMode(false)
        }
    }

    private val ambientObserver = AmbientLifecycleObserver(this, ambientCallback)

    override fun onCreate(savedInstanceState: Bundle?): Unit = trace("WearMainActivity.onCreate") {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(ambientObserver)

        var isSettingsLoaded = false
        splashScreen.setKeepOnScreenCondition { !isSettingsLoaded }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                settingsRepository.isLoaded.first { it }
                isSettingsLoaded = true

                binding = WearActivityMainBinding.inflate(layoutInflater)
                setContentView(binding.root)

                binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
                    override fun onDismissed(layout: SwipeDismissFrameLayout) {
                        finish()
                    }
                })

                val pagerBackCallback = object : OnBackPressedCallback(false) {
                    override fun handleOnBackPressed() {
                        binding.pager.currentItem--
                    }
                }
                onBackPressedDispatcher.addCallback(this@WearMainActivity, pagerBackCallback)

                val pagerAdapter = WearPagerAdapter(this@WearMainActivity)
                binding.pager.run {
                    adapter = pagerAdapter
                    offscreenPageLimit = 1

                    registerOnPageChangeCallback( object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            binding.swipeDismissRoot.isSwipeable = position == 0
                            pagerBackCallback.isEnabled = position > 0
                            // Ensure InfoType is in sync with the last viewed tab
                            if (position < InfoType.INFO_TYPE_COUNT) {
                                InfoType.currentInfoType = InfoType.fromInt(position)
                            }
                        }
                    })
                }

                viewModel.loadData(this@WearMainActivity)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshDetailsTrigger.collect {
                    for (i in 0 until (binding.pager.adapter?.itemCount ?: 0)) {
                        val fragment = supportFragmentManager.findFragmentByTag("f$i")
                        if (fragment is WearItemFragment) {
                            fragment.updateView()
                        }
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KEYCODE_STEM_1 && event.repeatCount == 0) {
            val detailsFragment = supportFragmentManager.findFragmentByTag(
                getString(R.string.details_fragment_tag)) as? DetailsFragment
            if (detailsFragment != null && detailsFragment.isVisible) {
                detailsFragment.shareCurrentDetails()
            } else {
                launchVoiceSearch()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun launchVoiceSearch() {
        val remoteInput = RemoteInput.Builder(EXTRA_QUERY)
            .setLabel(getString(R.string.action_search))
            .build()
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        remoteInputLanucher.launch(intent)
    }

    override fun onDestroy() {
        audioCodecList.clear()
        videoCodecList.clear()
        drmList.clear()
        detailedCodecInfos.clear()
        detailedDrmInfo.clear()
        TextSizeCache.clear()
        super.onDestroy()
    }

}