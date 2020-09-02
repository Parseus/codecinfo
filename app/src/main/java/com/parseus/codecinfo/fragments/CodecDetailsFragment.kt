package com.parseus.codecinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.adapters.CodecInfoAdapter
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.databinding.CodecDetailsFragmentLayoutBinding

class CodecDetailsFragment : Fragment() {

    private var _binding: CodecDetailsFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    var codecId: String? = null
    var codecName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = CodecDetailsFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            codecId = requireArguments().getString("codecId")
            codecName = requireArguments().getString("codecName")
            binding.fullCodecInfoName.text = codecName

            codecId?.let {
                val codecInfoMap = getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
                val codecAdapter = CodecInfoAdapter(codecInfoMap)
                binding.fullCodecInfoContent.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = codecAdapter
                    ViewCompat.setNestedScrollingEnabled(this, false)
                }
            }
        }
    }

}