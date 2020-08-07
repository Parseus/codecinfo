package com.parseus.codecinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.adapters.CodecAdapter
import com.parseus.codecinfo.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.databinding.TabContentLayoutBinding

class CodecFragment : Fragment() {

    private var _binding: TabContentLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = TabContentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val codecSimpleInfoList = getSimpleCodecInfoList(requireContext(), requireArguments().getBoolean("isAudio"))
        val codecAdapter = CodecAdapter(codecSimpleInfoList)
        binding.simpleCodecListView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = codecAdapter
            ViewCompat.setNestedScrollingEnabled(this, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

}