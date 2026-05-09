package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding
import com.parseus.codecinfo.utils.getColorOnSurfaceVariant
import com.parseus.codecinfo.utils.getSecondaryColor

class MobileDetailsAdapter(onHeaderClick: (Int) -> Unit) : DetailsAdapter(onHeaderClick) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_PROPERTY) {
            val binding = ItemDetailsAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MobileDetailsViewHolder(binding)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    class MobileDetailsViewHolder(binding: ItemDetailsAdapterRowBinding) : DetailsViewHolder(binding) {
        init {
            codecProperty.setTextColor(getSecondaryColor(codecProperty.context))
            codecValue.setTextColor(getColorOnSurfaceVariant(codecValue.context))
        }
    }

}
