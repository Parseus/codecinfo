package com.parseus.codecinfo.tv

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

class TvCodecDetailsFragment : Fragment() {

    private lateinit var binding: CodecDetailsFragmentLayoutBinding

    private var codecId: String? = null
    private var codecName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CodecDetailsFragmentLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().intent?.let {
            codecId = it.getStringExtra("codecId")
            codecName = it.getStringExtra("codecName")
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