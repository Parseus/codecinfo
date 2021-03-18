package com.parseus.codecinfo.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import com.parseus.codecinfo.databinding.TabContentLayoutBinding
import com.parseus.codecinfo.ui.adapters.CodecAdapter
import com.parseus.codecinfo.ui.adapters.DrmAdapter

internal var emptyListInformed = false

class ItemFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: TabContentLayoutBinding? = null
    private val binding get() = _binding!!

    private var emptyList = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabContentLayoutBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val infoType = InfoType.fromInt(requireArguments().getInt("infoType"))
        val itemAdapter = if (infoType != InfoType.DRM) {
            val codecSimpleInfoList = getSimpleCodecInfoList(requireContext(),
                    infoType == InfoType.Audio)
            if (codecSimpleInfoList.isEmpty()) emptyList = true
            CodecAdapter().also {
                if (!emptyList) {
                    it.add(codecSimpleInfoList)
                }
            }
        } else {
            val drmSimpleInfoList = getSimpleDrmInfoList(requireContext())
            if (drmSimpleInfoList.isEmpty()) emptyList = true
            DrmAdapter(drmSimpleInfoList)
        }

        if (!emptyList) {
            binding.simpleCodecListView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = itemAdapter
                ViewCompat.setNestedScrollingEnabled(this, false)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        } else if (!emptyListInformed) {
            // Do not spam the user with multiple snackbars.
            emptyListInformed = true
            val errorId = if (InfoType.currentInfoType != InfoType.DRM)
                R.string.unable_to_get_codec_info_error
                else R.string.unable_to_get_drm_info_error
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    errorId, Snackbar.LENGTH_LONG).show()
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
    private fun handleSearch(query: String) {
        if (emptyList) {
            return
        }

        val adapter = binding.simpleCodecListView.adapter
        if (adapter is CodecAdapter) {
            val fullList = getSimpleCodecInfoList(requireContext(), InfoType.currentInfoType == InfoType.Audio)
            adapter.replaceAll(filterCodecs(fullList, query))
        } else {
            (adapter as DrmAdapter)
            val fullList = getSimpleDrmInfoList(requireContext())
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