package com.parseus.codecinfo.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.EdgeEffect
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
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
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import com.parseus.codecinfo.databinding.TabContentLayoutBinding
import com.parseus.codecinfo.ui.CustomLinearLayoutManager
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.adapters.CodecAdapter
import com.parseus.codecinfo.ui.adapters.DrmAdapter
import com.parseus.codecinfo.ui.adapters.SearchListenerDestroyedListener
import com.parseus.codecinfo.utils.getSecondaryColor
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.updateColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemFragment : MonetFragment(), SearchView.OnQueryTextListener {

    private var _binding: TabContentLayoutBinding? = null
    private val binding get() = _binding!!

    private var emptyList = false
    private lateinit var infoType: InfoType
    private var searchJob: Job? = null
    private var itemAdapter: RecyclerView.Adapter<*>? = null

    var searchListenerDestroyedListener: SearchListenerDestroyedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = TabContentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        searchListenerDestroyedListener?.onSearchListenerDestroyed(this)
        searchListenerDestroyedListener = null

        if (activity as? MainActivity != null) {
            val searchListenerList = (activity as MainActivity).searchListeners
            searchListenerList.remove(this)
        }

        _binding = null

        super.onDestroyView()
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoType = InfoType.fromInt(requireArguments().getInt("infoType"))

        if (activity as? MainActivity != null) {
            val searchListenerList = (activity as MainActivity).searchListeners
            if (!searchListenerList.contains(this)) {
                searchListenerList.add(this)
            }
        }

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadAndDisplayData()
            }
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = if (infoType != InfoType.DRM) CodecAdapter() else DrmAdapter()
        binding.simpleCodecListView.apply {
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
    private suspend fun loadAndDisplayData() {
        val adapter = itemAdapter
        if (adapter is ListAdapter<*, *> && adapter.currentList.isNotEmpty()) {
            return
        }

        binding.loadingProgress.isVisible = true

        val list = withContext(Dispatchers.IO) {
            if (infoType != InfoType.DRM) {
                getSimpleCodecInfoList(requireContext(), infoType == InfoType.Audio)
            } else {
                getSimpleDrmInfoList(requireContext())
            }
        }

        emptyList = list.isEmpty()
        binding.loadingProgress.isVisible = false

        if (!emptyList) {
            when (adapter) {
                is CodecAdapter -> adapter.submitList(list as List<CodecSimpleInfo>) {
                    activity?.reportFullyDrawn()
                }
                is DrmAdapter -> adapter.submitList(list as List<DrmSimpleInfo>) {
                    activity?.reportFullyDrawn()
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
            activity?.reportFullyDrawn()
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

    @SuppressLint("NewApi")
    @Suppress("UNCHECKED_CAST")
    private fun handleSearch(query: String) {
        if (emptyList) return

        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            val filteredList = withContext(Dispatchers.Default) {
                when (itemAdapter) {
                    is CodecAdapter -> {
                        val fullList = getSimpleCodecInfoList(requireContext(), infoType == InfoType.Audio)
                        filterCodecs(fullList, query)
                    }
                    is DrmAdapter -> {
                        val fullList = getSimpleDrmInfoList(requireContext())
                        filterDrm(fullList, query)
                    }
                    else -> null
                }
            }

            filteredList?.let {
                when (val adapter = itemAdapter) {
                    is CodecAdapter -> adapter.submitList(it as List<CodecSimpleInfo>)
                    is DrmAdapter -> adapter.submitList(it as List<DrmSimpleInfo>)
                }
            }
        }
    }

    private fun filterCodecs(infoList: List<CodecSimpleInfo>, query: String): List<CodecSimpleInfo> {
        return infoList.filter {
            it.codecId.contains(query, true) || it.codecName.contains(query, true)
        }
    }

    private fun filterDrm(infoList: List<DrmSimpleInfo>, query: String): List<DrmSimpleInfo> {
        return infoList.filter {
            it.drmName.contains(query, true)
        }
    }

}