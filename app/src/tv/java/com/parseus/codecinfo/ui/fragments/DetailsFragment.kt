package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.isDetailedCodecInfoCached
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.isDetailedDrmInfoCached
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.databinding.ItemDetailsFragmentLayoutBinding
import com.parseus.codecinfo.ui.CustomLinearLayoutManager
import com.parseus.codecinfo.ui.adapters.DetailItem
import com.parseus.codecinfo.ui.adapters.DetailsItemDecoration
import com.parseus.codecinfo.ui.adapters.DetailsAdapter
import com.parseus.codecinfo.ui.expandablelist.ExpandableItemAnimator
import com.parseus.codecinfo.utils.getSelectedCodecInfoString
import com.parseus.codecinfo.utils.getSelectedDrmInfoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DetailsFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: ItemDetailsFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var propertyList: List<DetailsProperty>
    private var knownProblems: List<KnownProblem> = emptyList()
    private var isKnownProblemsExpanded = true
    private lateinit var detailsAdapter: DetailsAdapter

    private var codecId: String? = null
    private var codecName: String? = null

    private var drmName: String? = null
    private var drmUuid: UUID? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ItemDetailsFragmentLayoutBinding.inflate(inflater, container, false)
        binding.share.setOnClickListener {
            val textToShare = when {
                codecId != null && codecName != null -> getSelectedCodecInfoString(requireContext(),
                    codecId!!, codecName!!)
                drmName != null && drmUuid != null -> getSelectedDrmInfoString(requireContext(),
                    drmName!!, drmUuid!!)
                else -> ""
            }
            val shareIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textToShare)
                val title = if (codecId != null && codecName != null) {
                    "${getString(R.string.codec_details)}: $codecName"
                } else {
                    "${getString(R.string.drm_details)}: $drmName"
                }
                putExtra(Intent.EXTRA_TITLE, title)
            }, null)
            startActivity(shareIntent)
        }
        return binding.root
    }

    override fun onDestroyView() {
        binding.itemDetailsRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().intent?.let {
            codecId = it.getStringExtra("codecId")
            codecName = it.getStringExtra("codecName")
            drmName = it.getStringExtra("drmName")
            drmUuid = IntentCompat.getSerializableExtra(it, "drmUuid", UUID::class.java)
        }

        detailsAdapter = DetailsAdapter { _ ->
            isKnownProblemsExpanded = !isKnownProblemsExpanded
            updateFullDetailsList()
        }

        binding.itemDetailsRecyclerView.apply {
            layoutManager = CustomLinearLayoutManager(context)
            adapter = detailsAdapter
            addItemDecoration(DetailsItemDecoration(context))
            itemAnimator = ExpandableItemAnimator()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

                knownProblems = if (codecName != null && KNOWN_PROBLEMS_DB.isNotEmpty()) {
                    KNOWN_PROBLEMS_DB.filter {
                        it.isAffected(requireContext(), codecName!!)
                    }
                } else {
                    emptyList()
                }

                binding.loadingProgress.isVisible = false
                getFullDetails()
            }
        }
    }

    private fun getFullDetails() {
        binding.fullCodecInfoName.text = codecName ?: drmName
        updateFullDetailsList()
    }

    private fun updateFullDetailsList(filteredProperties: List<DetailsProperty>? = null) {
        val fullList = mutableListOf<DetailItem>()
        val currentProperties = filteredProperties ?: propertyList
        
        // Hide known problems if searching
        if (knownProblems.isNotEmpty() && (filteredProperties == null || filteredProperties.size == propertyList.size)) {
            fullList.add(DetailItem.Header(0L, R.string.known_issue_warning, isKnownProblemsExpanded))
            if (isKnownProblemsExpanded) {
                knownProblems.forEach {
                    fullList.add(DetailItem.KnownProblemItem(it))
                }
            }
        }
        
        currentProperties.forEach {
            fullList.add(DetailItem.PropertyItem(it))
        }
        detailsAdapter.submitList(fullList)
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (isVisible) {
            handleSearch(newText)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (isVisible) {
            handleSearch(query)
        }
        return true
    }

    private fun handleSearch(query: String) {
        updateFullDetailsList(filterProperties(query))
    }

    private fun filterProperties(query: String): List<DetailsProperty> {
        return propertyList.filter { (_, name, value) ->
            name.contains(query, true) || value.contains(query, true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("codecId", codecId)
        outState.putString("codecName", codecName)
        outState.putString("drmName", drmName)
        outState.putSerializable("drmUuid", drmUuid)
    }

}
