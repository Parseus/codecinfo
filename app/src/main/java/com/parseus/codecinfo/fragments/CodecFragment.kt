package com.parseus.codecinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.adapters.CodecAdapter
import com.parseus.codecinfo.codecinfo.getSimpleCodecInfoList
import kotlinx.android.synthetic.main.tab_content_layout.*

class CodecFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tab_content_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val codecSimpleInfoList = getSimpleCodecInfoList(requireContext(), requireArguments().getBoolean("isAudio"))
        val codecAdapter = CodecAdapter(codecSimpleInfoList)
        simpleCodecListView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = codecAdapter
            ViewCompat.setNestedScrollingEnabled(this, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

}