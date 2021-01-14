package com.parseus.codecinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.InfoType
import com.parseus.codecinfo.adapters.CodecAdapter
import com.parseus.codecinfo.adapters.DrmAdapter
import com.parseus.codecinfo.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.databinding.TabContentLayoutBinding
import com.parseus.codecinfo.drm.getSimpleDrmInfoList

class ItemFragment : Fragment() {

    private var _binding: TabContentLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabContentLayoutBinding.inflate(inflater, container, false)
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
            val codecSimpleInfoList = getSimpleCodecInfoList(requireContext(), infoType == InfoType.Audio)
            CodecAdapter(codecSimpleInfoList)
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

}