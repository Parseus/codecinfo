package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding
import com.parseus.codecinfo.utils.getSecondaryColor

class MobileDetailsAdapter : DetailsAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val binding = ItemDetailsAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MobileDetailsViewHolder(binding)
    }

    class MobileDetailsViewHolder(binding: ItemDetailsAdapterRowBinding) : DetailsViewHolder(binding) {
        override fun bindDetails(name: String, info: String) {
            super.bindDetails(name, info)
            codecName.setTextColor(getSecondaryColor(codecName.context))
        }
    }

}