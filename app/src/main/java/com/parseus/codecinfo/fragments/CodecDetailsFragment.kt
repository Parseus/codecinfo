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

    var codecId = ""
    var codecName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CodecDetailsFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = savedInstanceState ?: arguments

        bundle?.let {
            codecId = it.getString("codecId")!!
            codecName = it.getString("codecName")!!
            getFullDetails()
        }
    }

    private fun getFullDetails() {
        if (codecId.isNotEmpty() && codecName.isNotEmpty()) {
            binding.fullCodecInfoName.text = codecName

            val codecInfoMap = getDetailedCodecInfo(requireContext(), codecId, codecName)
            val codecAdapter = CodecInfoAdapter(codecInfoMap)
            binding.fullCodecInfoContent.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = codecAdapter
                ViewCompat.setNestedScrollingEnabled(this, false)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("codecId", codecId)
        outState.putString("codecName", codecName)
    }

}