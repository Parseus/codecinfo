package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding

open class DetailsAdapter : ListAdapter<DetailsProperty, DetailsAdapter.DetailsViewHolder>(
    DetailsDiffCallback()
) {

    fun replaceAll(infoList: List<DetailsProperty>) {
        submitList(infoList.sortedBy { it.id })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val binding = ItemDetailsAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val property = getItem(position)
        val name = property.name
        val info = property.value
        holder.bindDetails(name, info)
    }

    open class DetailsViewHolder(binding: ItemDetailsAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        protected val codecName = binding.codecProperty
        protected val codecInfo = binding.codecValue as AppCompatTextView

        @CallSuper
        open fun bindDetails(name: String, info: String) {
            codecName.text = name
            codecInfo.setTextFuture(
                PrecomputedTextCompat.getTextFuture(info,
                    codecInfo.textMetricsParamsCompat, null)
            )
        }

    }

    private class DetailsDiffCallback : DiffUtil.ItemCallback<DetailsProperty>() {
        override fun areItemsTheSame(oldItem: DetailsProperty, newItem: DetailsProperty) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DetailsProperty, newItem: DetailsProperty) = oldItem == newItem
    }

}