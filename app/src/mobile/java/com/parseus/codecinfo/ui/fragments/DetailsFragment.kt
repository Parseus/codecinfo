package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.kieronquinn.monetcompat.app.MonetFragment
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.isDetailedCodecInfoCached
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.isDetailedDrmInfoCached
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.databinding.ItemDetailsFragmentLayoutBinding
import com.parseus.codecinfo.ui.CustomLinearLayoutManager
import com.parseus.codecinfo.ui.adapters.DetailsAdapter
import com.parseus.codecinfo.ui.adapters.MobileDetailsAdapter
import com.parseus.codecinfo.ui.adapters.SearchListenerDestroyedListener
import com.parseus.codecinfo.ui.expandablelist.ExpandableItemAdapter
import com.parseus.codecinfo.ui.expandablelist.ExpandableItemAnimator
import com.parseus.codecinfo.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DetailsFragment : MonetFragment(), SearchView.OnQueryTextListener {

    private var _binding: ItemDetailsFragmentLayoutBinding? = null
    internal val binding get() = _binding!!

    var searchListenerDestroyedListener: SearchListenerDestroyedListener? = null

    private lateinit var propertyList: List<DetailsProperty>

    var codecId: String? = null
    var codecName: String? = null

    var drmName: String? = null
    var drmUuid: UUID? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = ItemDetailsFragmentLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        searchListenerDestroyedListener?.onSearchListenerDestroyed(this)
        searchListenerDestroyedListener = null
        binding.itemDetailsContent.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener?)
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
            view.applyMonetRecursively()
        }

        binding.loadingProgress.updateColors(requireContext())

        if (!requireContext().isInTwoPaneMode()) {
            // Apply background color only on mobile to reduce overdraw on bigger devices
            binding.endRoot.setBackgroundColor(getSurfaceColor(requireContext()))
        }

        val bundle = savedInstanceState ?: arguments
        bundle?.let {
            codecId = it.getString("codecId")
            codecName = it.getString("codecName")
            drmName = it.getString("drmName")
            drmUuid = BundleCompat.getSerializable(it, "drmUuid", UUID::class.java)
        }

        binding.itemDetailsContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
                _, _, scrollY, _, _ -> binding.fullCodecInfoName.isHeaderLifted = scrollY > 0
        })

        if (codecName != null && KNOWN_PROBLEMS_DB.isNotEmpty()) {
            val knownProblems = KNOWN_PROBLEMS_DB.filter {
                it.isAffected(requireContext(), codecName!!)
            }
            if (knownProblems.isNotEmpty()) {
                binding.knownProblemsList.apply {
                    layoutManager = CustomLinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
                    itemAnimator = ExpandableItemAnimator()
                    isVisible = true
                    adapter = ExpandableItemAdapter(knownProblems)
                }
            }
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

                binding.loadingProgress.isVisible = false
                showFullDetails()
            }
        }
    }

    private fun showFullDetails() {
        binding.fullCodecInfoName.text = codecName ?: drmName
        binding.fullCodecInfoName.setTextColor(getPrimaryColor(requireContext()))

        val detailsAdapter = MobileDetailsAdapter()
        detailsAdapter.add(propertyList)
        binding.fullCodecInfoContent.apply {
            layoutManager = CustomLinearLayoutManager(context)
            adapter = detailsAdapter
            isNestedScrollingEnabled = false
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