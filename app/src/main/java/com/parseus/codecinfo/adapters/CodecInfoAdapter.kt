package com.parseus.codecinfo.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.bind
import com.parseus.codecinfo.inflate

class CodecInfoAdapter(private val codecInfoMap: Map<String, String>) : RecyclerView.Adapter<CodecInfoAdapter.CodecInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodecInfoViewHolder {
        val inflatedView = parent.inflate(R.layout.codec_info_adapter_row)
        return CodecInfoViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: CodecInfoViewHolder, position: Int) {
        val name = codecInfoMap.keys.elementAt(position)
        val info = codecInfoMap.getValue(name)
        holder.bindCodecInfo(name, info)
    }

    override fun getItemCount(): Int = codecInfoMap.size

    class CodecInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val codecName by view.bind<TextView>(R.id.codec_property)
        private val codecInfo by view.bind<TextView>(R.id.codec_value)

        fun bindCodecInfo(name: String, info: String) {
            codecName.text = name
            codecInfo.text = info
        }

    }
}