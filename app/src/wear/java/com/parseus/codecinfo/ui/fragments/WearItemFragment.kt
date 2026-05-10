package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.databinding.WearFragmentItemBinding
import com.parseus.codecinfo.ui.adapters.WearCodecAdapter
import com.parseus.codecinfo.ui.adapters.WearDrmAdapter
import com.parseus.codecinfo.ui.adapters.WearSearchHeaderAdapter
import com.parseus.codecinfo.utils.applyRotaryInput
import com.parseus.codecinfo.viewmodels.ItemsViewModel
import kotlinx.coroutines.launch

class WearItemFragment : Fragment() {

    private var _binding: WearFragmentItemBinding? = null
    private val binding get() = _binding!!

    private var emptyList = false
    private lateinit var infoType: InfoType
    private var itemAdapter: RecyclerView.Adapter<*>? = null
    private val searchHeaderAdapter = WearSearchHeaderAdapter()

    private val viewModel: ItemsViewModel by activityViewModels()

    private var isFullyDrawnReporterAdded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearFragmentItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoType = InfoType.fromInt(requireArguments().getInt(INFO_TYPE))

        itemAdapter = if (infoType != InfoType.DRM) WearCodecAdapter() else WearDrmAdapter()
        val concatAdapter = ConcatAdapter(searchHeaderAdapter, itemAdapter)
        binding.wearableRecyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            adapter = concatAdapter
            layoutManager = WearableLinearLayoutManager(requireContext())
            applyRotaryInput()
            requestFocus()
        }

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
            binding.loadingProgress.isVisible = !viewModel.isAmbientMode.value
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isAmbientMode.collect { isAmbient ->
                    if (isAmbient) {
                        binding.loadingProgress.isVisible = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (infoType) {
                    InfoType.Audio -> {
                        viewModel.updateAudioList(requireContext())
                        viewModel.allAudioState.collect { it?.let {
                            (itemAdapter as? ListAdapter<*, *>)?.submitList(null)
                            displayData(it)
                        } }
                    }
                    InfoType.Video -> {
                        viewModel.updateVideoList(requireContext())
                        viewModel.allVideoState.collect { it?.let {
                            (itemAdapter as? ListAdapter<*, *>)?.submitList(null)
                            displayData(it)
                        } }
                    }
                    InfoType.DRM -> {
                        viewModel.updateDrmList(requireContext())
                        viewModel.allDrmsState.collect { it?.let {
                            (itemAdapter as? ListAdapter<*, *>)?.submitList(null)
                            displayData(it)
                        } }
                    }
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
            binding.wearableRecyclerView.isVisible = true
            binding.noItemsAvailable.isVisible = false
            when (adapter) {
                is WearCodecAdapter -> adapter.submitList(list as List<CodecSimpleInfo>) {
                    removeFullyDrawnReporter()
                }
                is WearDrmAdapter -> adapter.submitList(list as List<DrmSimpleInfo>) {
                    removeFullyDrawnReporter()
                }
            }
        } else {
            binding.wearableRecyclerView.isVisible = false
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

    override fun onResume() {
        super.onResume()
        binding.wearableRecyclerView.requestFocus()
    }

    override fun onDestroyView() {
        removeFullyDrawnReporter()

        binding.wearableRecyclerView.adapter = null
        _binding = null
        itemAdapter = null

        super.onDestroyView()
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

    companion object {
        private const val INFO_TYPE = "infoType"

        fun newInstance(infoType: InfoType) = WearItemFragment().apply {
            arguments = Bundle().apply { putInt(INFO_TYPE, infoType.tabPosition) }
        }
    }
}
