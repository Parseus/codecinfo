package com.parseus.codecinfo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding

class DetailsAdapter(private val infoMap: Map<String, String>) : RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val binding = ItemDetailsAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val name = infoMap.keys.elementAt(position)
        val info = infoMap.getValue(name)
        holder.bindDetails(name, info)
    }

    override fun getItemCount(): Int = infoMap.size

    class DetailsViewHolder(binding: ItemDetailsAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val codecName = binding.codecProperty
        private val codecInfo = binding.codecValue

        fun bindDetails(name: String, info: String) {
            codecName.text = name
            codecInfo.text = info
        }

    }
}