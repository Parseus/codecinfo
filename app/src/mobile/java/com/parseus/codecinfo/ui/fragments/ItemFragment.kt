package com.parseus.codecinfo.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.kieronquinn.monetcompat.app.MonetFragment
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.databinding.TabContentLayoutBinding
import com.parseus.codecinfo.ui.CustomLinearLayoutManager
import com.parseus.codecinfo.ui.adapters.CodecAdapter
import com.parseus.codecinfo.ui.adapters.DrmAdapter
import com.parseus.codecinfo.utils.getSecondaryColor
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.updateColors
import com.parseus.codecinfo.viewmodels.ItemsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ItemFragment : MonetFragment() {

    private var _binding: TabContentLayoutBinding? = null
    private val binding get() = _binding!!

    private var emptyList = false
    private lateinit var infoType: InfoType
    private var searchJob: Job? = null
    private var itemAdapter: RecyclerView.Adapter<*>? = null

    private val viewModel: ItemsViewModel by activityViewModels()

    private var isFullyDrawnReporterAdded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = TabContentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        removeFullyDrawnReporter()

        searchJob?.cancel()
        searchJob = null
        binding.simpleCodecListView.adapter = null
        itemAdapter = null
        _binding = null

        super.onDestroyView()
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoType = InfoType.fromInt(requireArguments().getInt("infoType"))

        if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
            view.applyMonetRecursively()
        }

        binding.loadingProgress.updateColors(requireContext())

        ViewCompat.setOnApplyWindowInsetsListener(binding.simpleCodecListView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        view.requestApplyInsets()

        setupRecyclerView()

        addFullyDrawnReporter()

        val currentList = when (infoType) {
            InfoType.Audio -> viewModel.allAudioState.value
            InfoType.Video -> viewModel.allVideoState.value
            InfoType.DRM -> viewModel.allDrmsState.value
        }

        if (currentList != null) {
            binding.loadingProgress.isVisible = false
            displayData(currentList)
        } else {
            binding.loadingProgress.isVisible = true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (infoType) {
                    InfoType.Audio -> {
                        viewModel.updateAudioList(requireContext())
                        viewModel.allAudioState.collect {
                            it?.let {
                                (itemAdapter as? ListAdapter<*, *>)?.submitList(null)
                                displayData(it)
                            }
                        }
                    }
                    InfoType.Video -> {
                        viewModel.updateVideoList(requireContext())
                        viewModel.allVideoState.collect {
                            it?.let {
                                (itemAdapter as? ListAdapter<*, *>)?.submitList(null)
                                displayData(it)
                            }
                        }
                    }
                    InfoType.DRM -> {
                        viewModel.updateDrmList(requireContext())
                        viewModel.allDrmsState.collect {
                            it?.let {
                                (itemAdapter as? ListAdapter<*, *>)?.submitList(null)
                                displayData(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = if (infoType != InfoType.DRM) CodecAdapter() else DrmAdapter()
        binding.simpleCodecListView.apply {
            setHasFixedSize(true)
            layoutManager = CustomLinearLayoutManager(context)
            adapter = itemAdapter
            isNestedScrollingEnabled = false
            addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
            edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                    return EdgeEffect(view.context).apply { color = getSecondaryColor(view.context) }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun displayData(list: List<Any>) {
        val adapter = itemAdapter
        emptyList = list.isEmpty()
        binding.loadingProgress.isVisible = false

        if (!emptyList) {
            binding.simpleCodecListView.isVisible = true
            binding.noItemsAvailable.isVisible = false
            when (adapter) {
                is CodecAdapter -> adapter.submitList(list as List<CodecSimpleInfo>) {
                    removeFullyDrawnReporter()
                }
                is DrmAdapter -> adapter.submitList(list as List<DrmSimpleInfo>) {
                    removeFullyDrawnReporter()
                }
            }
        } else {
            binding.simpleCodecListView.isVisible = false
            binding.noItemsAvailable.isVisible = true
            val errorId = if (infoType != InfoType.DRM) {
                R.string.no_codecs_available
            } else {
                R.string.no_drms_available
            }
            binding.noItemsAvailable.setText(errorId)
            removeFullyDrawnReporter()
        }
    }

    fun updateView() {
        if (!::infoType.isInitialized) return

        val currentList = when (infoType) {
            InfoType.Audio -> viewModel.allAudioState.value
            InfoType.Video -> viewModel.allVideoState.value
            InfoType.DRM -> viewModel.allDrmsState.value
        }
        currentList?.let {
            (itemAdapter as? ListAdapter<*, *>)?.submitList(null)
            displayData(it)
        }
    }

    private fun addFullyDrawnReporter() {
        if (!isFullyDrawnReporterAdded) {
            requireActivity().fullyDrawnReporter.addReporter()
            isFullyDrawnReporterAdded = true
        }
    }

    private fun removeFullyDrawnReporter() {
        if (isFullyDrawnReporterAdded) {
            activity?.fullyDrawnReporter?.removeReporter()
            isFullyDrawnReporterAdded = false
        }
    }

}