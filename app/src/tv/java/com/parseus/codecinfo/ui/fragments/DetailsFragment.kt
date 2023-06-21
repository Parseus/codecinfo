package com.parseus.codecinfo.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.databinding.ItemDetailsFragmentLayoutBinding
import com.parseus.codecinfo.ui.adapters.DetailsAdapter
import com.parseus.codecinfo.ui.expandablelist.ExpandableItemAdapter
import com.parseus.codecinfo.ui.expandablelist.ExpandableItemAnimator
import java.util.*

class DetailsFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: ItemDetailsFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var propertyList: List<DetailsProperty>

    private var codecId: String? = null
    private var codecName: String? = null

    private var drmName: String? = null
    private var drmUuid: UUID? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ItemDetailsFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().intent?.let {
            codecId = it.getStringExtra("codecId")
            codecName = it.getStringExtra("codecName")
            drmName = it.getStringExtra("drmName")
            drmUuid = if (Build.VERSION.SDK_INT >= 33) {
                it.getSerializableExtra("drmUuid", UUID::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializableExtra("drmUuid") as UUID?
            }
        }

        if (codecName != null && KNOWN_PROBLEMS_DB.isNotEmpty()) {
            val knownProblems = KNOWN_PROBLEMS_DB.filter {
                it.isAffected(requireContext(), codecName!!)
            }
            if (knownProblems.isNotEmpty()) {
                binding.knownProblemsList.apply {
                    layoutManager = LinearLayoutManager(context)
                    ViewCompat.setNestedScrollingEnabled(this, false)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                    itemAnimator = ExpandableItemAnimator()
                    isVisible = true
                    adapter = ExpandableItemAdapter(knownProblems)
                }
            }
        }

        propertyList = when {
            codecId != null && codecName != null ->
                getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
            drmName != null && drmUuid != null ->
                getDetailedDrmInfo(requireContext(), drmUuid!!, DrmVendor.getFromUuid(drmUuid!!))
            else -> emptyList()
        }
        getFullDetails()
    }

    private fun getFullDetails() {
        binding.fullCodecInfoName.text = codecName ?: drmName

        val detailsAdapter = DetailsAdapter()
        detailsAdapter.add(propertyList)
        binding.fullCodecInfoContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailsAdapter
            ViewCompat.setNestedScrollingEnabled(this, false)
        }
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
        val adapter = binding.fullCodecInfoContent.adapter as DetailsAdapter
        adapter.replaceAll(filterProperties(query))
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