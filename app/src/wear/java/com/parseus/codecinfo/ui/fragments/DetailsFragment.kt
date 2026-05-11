package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.tracing.trace
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.isDetailedCodecInfoCached
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.isDetailedDrmInfoCached
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.data.knownproblems.loadDatabases
import com.parseus.codecinfo.databinding.WearItemDetailsFragmentLayoutBinding
import com.parseus.codecinfo.ui.adapters.DetailItem
import com.parseus.codecinfo.ui.adapters.WearDetailsAdapter
import com.parseus.codecinfo.ui.adapters.WearShareActionAdapter
import com.parseus.codecinfo.utils.applyRotaryInput
import com.parseus.codecinfo.utils.getSelectedCodecInfoString
import com.parseus.codecinfo.utils.getSelectedDrmInfoString
import com.parseus.codecinfo.utils.hasSecondaryButton
import com.parseus.codecinfo.viewmodels.ItemsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DetailsFragment : Fragment() {

    private var _binding: WearItemDetailsFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var propertyList: List<DetailsProperty>
    private var knownProblems: List<KnownProblem> = emptyList()
    private var isKnownProblemsExpanded = true
    private lateinit var detailsAdapter: WearDetailsAdapter
    private lateinit var shareAdapter: WearShareActionAdapter

    private var codecId: String? = null
    private var codecName: String? = null
    private var drmName: String? = null
    private var drmUuid: UUID? = null

    private var isFullyDrawnReporterAdded = false

    private val viewModel: ItemsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearItemDetailsFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addFullyDrawnReporter()

        val bundle = savedInstanceState ?: arguments
        bundle?.let {
            codecId = it.getString("codecId")
            codecName = it.getString("codecName")
            drmName = it.getString("drmName")
            drmUuid = BundleCompat.getSerializable(it, "drmUuid", UUID::class.java)
        }

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                parentFragmentManager.popBackStack()
            }
        })

        if (!requireContext().hasSecondaryButton()) {
            binding.shareFab.isVisible = true
            binding.shareFab.setOnClickListener { shareCurrentDetails() }
        }

        shareAdapter = WearShareActionAdapter {
            shareCurrentDetails()
        }
        detailsAdapter = WearDetailsAdapter {
            isKnownProblemsExpanded = !isKnownProblemsExpanded
            updateFullDetailsList()
        }
        val concatAdapter = ConcatAdapter(detailsAdapter, shareAdapter)
        binding.itemDetailsRecyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(requireContext())
            adapter = concatAdapter
            applyRotaryInput()
            requestFocus()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.refreshDetailsTrigger.collect {
                        loadDetails()
                    }
                }

                loadDetails()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isAmbientMode.collect { isAmbient ->
                    binding.loadingProgress.isVisible = !isAmbient
                }
            }
        }
    }

    private suspend fun loadDetails() = trace("DetailsFragment.loadDetails") {
        binding.loadingProgress.isVisible = true

        propertyList = if (codecId != null && codecName != null && isDetailedCodecInfoCached(codecId!!, codecName!!)) {
            getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
        } else if (drmName != null && drmUuid != null && isDetailedDrmInfoCached(drmUuid!!)) {
            getDetailedDrmInfo(requireContext(), drmUuid!!, DrmVendor.getFromUuid(drmUuid!!))
        } else {
            withContext(Dispatchers.IO) {
                when {
                    codecId != null && codecName != null ->
                        getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
                    drmName != null && drmUuid != null ->
                        getDetailedDrmInfo(requireContext(), drmUuid!!, DrmVendor.getFromUuid(drmUuid!!))
                    else -> emptyList()
                }
            }
        }

        if (codecName != null) {
            loadDatabases(requireContext())
        }
        knownProblems = if (codecName != null && KNOWN_PROBLEMS_DB.isNotEmpty()) {
            KNOWN_PROBLEMS_DB.filter {
                it.isAffected(requireContext(), codecName!!)
            }
        } else {
            emptyList()
        }

        binding.loadingProgress.isVisible = false
        updateFullDetailsList()
        removeFullyDrawnReporter()
    }

    private fun updateFullDetailsList() {
        val fullList = mutableListOf<DetailItem>()
        if (knownProblems.isNotEmpty()) {
            fullList.add(DetailItem.Header(0L, R.string.known_issue_warning, isKnownProblemsExpanded))
            if (isKnownProblemsExpanded) {
                knownProblems.forEach {
                    fullList.add(DetailItem.KnownProblemItem(it))
                }
            }
        }
        propertyList.forEach {
            fullList.add(DetailItem.PropertyItem(it))
        }
        detailsAdapter.submitList(fullList)
    }

    fun shareCurrentDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            val (textToShare, title) = withContext(Dispatchers.IO) {
                when {
                    codecId != null && codecName != null -> {
                        getSelectedCodecInfoString(requireContext(), codecId!!, codecName!!) to "${getString(R.string.codec_details)}: $codecName"
                    }
                    drmName != null && drmUuid != null -> {
                        getSelectedDrmInfoString(requireContext(), drmName!!, drmUuid!!) to "${getString(R.string.drm_details)}: $drmName"
                    }
                    else -> "" to ""
                }
            }
            if (textToShare.isNotEmpty()) {
                val shareIntent = Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textToShare)
                    putExtra(Intent.EXTRA_TITLE, title)
                }, null)
                startActivity(shareIntent)
            }
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

    override fun onDestroyView() {
        removeFullyDrawnReporter()
        binding.itemDetailsRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("codecId", codecId)
        outState.putString("codecName", codecName)
        outState.putString("drmName", drmName)
        outState.putSerializable("drmUuid", drmUuid)
    }
}