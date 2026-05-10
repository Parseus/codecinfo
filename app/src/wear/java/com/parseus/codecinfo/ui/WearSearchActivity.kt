package com.parseus.codecinfo.ui

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.WearActivitySearchBinding
import com.parseus.codecinfo.ui.adapters.WearCodecAdapter
import com.parseus.codecinfo.ui.adapters.WearDrmAdapter
import com.parseus.codecinfo.utils.applyRotaryInput
import com.parseus.codecinfo.viewmodels.SearchViewModel
import kotlinx.coroutines.launch

class WearSearchActivity : AppCompatActivity() {

    private lateinit var binding: WearActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.ime())

            binding.searchEditText.clearFocus()

            viewModel.setAmbientMode(true)
        }

        override fun onExitAmbient() {
            viewModel.setAmbientMode(false)
        }
    }
    private val ambientObserver = AmbientLifecycleObserver(this, ambientCallback)

    private val remoteInputLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val results = RemoteInput.getResultsFromIntent(result.data)
            val query = results?.getCharSequence(EXTRA_QUERY)?.toString()
            if (!query.isNullOrBlank()) {
                binding.searchEditText.setText(query)
                viewModel.setSearchQuery(query)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(ambientObserver)
        binding = WearActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                finish()
            }
        })

        val audioAdapter = WearCodecAdapter()
        val videoAdapter = WearCodecAdapter()
        val drmAdapter = WearDrmAdapter()
        val concatAdapter = ConcatAdapter(audioAdapter, videoAdapter, drmAdapter)

        binding.wearableRecyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@WearSearchActivity)
            adapter = concatAdapter
            applyRotaryInput()
            requestFocus()
        }

        binding.searchEditText.setOnClickListener { launchRemoteInput() }

        binding.searchEditText.addTextChangedListener {
            viewModel.setSearchQuery(it.toString())
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isAmbientMode.collect { isAmbient ->
                    if (isAmbient) {
                        binding.loadingProgress.isVisible = false
                        binding.searchEditText.clearFocus()
                    }
                }
            }
        }

        viewModel.initData(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultState.collect { state ->
                    if (state.isQueryEmpty) {
                        audioAdapter.submitList(emptyList())
                        videoAdapter.submitList(emptyList())
                        drmAdapter.submitList(emptyList())
                    } else {
                        audioAdapter.submitList(state.audioResults)
                        videoAdapter.submitList(state.videoResults)
                        drmAdapter.submitList(state.drmResults)
                    }
                }
            }
        }

        val queryFromIntent = intent.getStringExtra("query")
        if (!queryFromIntent.isNullOrBlank()) {
            binding.searchEditText.setText(queryFromIntent)
            viewModel.setSearchQuery(queryFromIntent)
        } else if (intent.action == Intent.ACTION_MAIN) {
            // Automatically launch RemoteInput if no query is provided (optional UX choice)
            launchRemoteInput()
        }
    }

    private fun launchRemoteInput() {
        // Should be similar to those prioritized by pre-caching in ItemsViewModel.

        val remoteInput = RemoteInput.Builder(EXTRA_QUERY)
            .setLabel(getString(R.string.search_hint))
            .setChoices(HIGH_PRIORITY_SEARCHES)
            .build()
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        remoteInputLauncher.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        binding.wearableRecyclerView.requestFocus()
    }

    override fun onDestroy() {
        binding.wearableRecyclerView.adapter = null
        super.onDestroy()
    }

    companion object {
        private val HIGH_PRIORITY_SEARCHES = arrayOf("AVC", "HEVC", "AV1", "VP9", "AAC", "MP3", "Clearkey", "Widevine")
        const val EXTRA_QUERY = "extra_query"
    }
}