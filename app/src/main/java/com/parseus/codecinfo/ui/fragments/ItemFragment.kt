package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import com.parseus.codecinfo.databinding.TabContentLayoutBinding
import com.parseus.codecinfo.ui.adapters.CodecAdapter
import com.parseus.codecinfo.ui.adapters.DrmAdapter

class ItemFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: TabContentLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabContentLayoutBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val infoType = InfoType.fromInt(requireArguments().getInt("infoType"))
        val itemAdapter = if (infoType != InfoType.DRM) {
            CodecAdapter().also { it.add(getSimpleCodecInfoList(requireContext(),
                    infoType == InfoType.Audio)) }
        } else {
            val drmSimpleInfoList = getSimpleDrmInfoList()
            DrmAdapter(drmSimpleInfoList)
        }

        binding.simpleCodecListView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
            ViewCompat.setNestedScrollingEnabled(this, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
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
        val adapter = binding.simpleCodecListView.adapter
        if (adapter is CodecAdapter) {
            val fullList = getSimpleCodecInfoList(requireContext(), InfoType.currentInfoType == InfoType.Audio)
            adapter.replaceAll(filterCodecs(fullList, query))
        } else {
            (adapter as DrmAdapter)
            val fullList = getSimpleDrmInfoList()
            adapter.replaceAll(filterDrm(fullList, query))
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