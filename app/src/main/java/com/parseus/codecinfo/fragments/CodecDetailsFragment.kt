package com.parseus.codecinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.adapters.CodecInfoAdapter
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo
import kotlinx.android.synthetic.main.codec_details_fragment_layout.*

class CodecDetailsFragment : Fragment() {

    var codecId: String? = null
    var codecName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.codec_details_fragment_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            codecId = arguments!!.getString("codecId")
            codecName = arguments!!.getString("codecName")
            full_codec_info_name.text = codecName

            codecId?.let {
                val codecInfoMap = getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
                val codecAdapter = CodecInfoAdapter(codecInfoMap)
                full_codec_info_content.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = codecAdapter
                    ViewCompat.setNestedScrollingEnabled(this, false)
                }
            }
        }
    }

}