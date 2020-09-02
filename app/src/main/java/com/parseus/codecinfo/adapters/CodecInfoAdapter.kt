package com.parseus.codecinfo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.databinding.CodecInfoAdapterRowBinding

class CodecInfoAdapter(private val codecInfoMap: Map<String, String>) : RecyclerView.Adapter<CodecInfoAdapter.CodecInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodecInfoViewHolder {
        val binding = CodecInfoAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CodecInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CodecInfoViewHolder, position: Int) {
        val name = codecInfoMap.keys.elementAt(position)
        val info = codecInfoMap.getValue(name)
        holder.bindCodecInfo(name, info)
    }

    override fun getItemCount(): Int = codecInfoMap.size

    class CodecInfoViewHolder(binding: CodecInfoAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val codecName = binding.codecProperty
        private val codecInfo = binding.codecValue

        fun bindCodecInfo(name: String, info: String) {
            codecName.text = name
            codecInfo.text = info
        }

    }
}