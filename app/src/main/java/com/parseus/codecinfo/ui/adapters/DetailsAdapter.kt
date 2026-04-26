package com.parseus.codecinfo.ui.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.PointerIcon
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

            if (Build.VERSION.SDK_INT >= 24) {
                itemView.setOnLongClickListener { v ->
                    val textToDrag = "$name: $info"
                    val item = ClipData.Item(textToDrag)
                    val dragData = ClipData(textToDrag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                    v.startDragAndDrop(dragData, View.DragShadowBuilder(v), null, View.DRAG_FLAG_GLOBAL)
                }
                itemView.pointerIcon = PointerIcon.getSystemIcon(itemView.context, PointerIcon.TYPE_HAND)
            }
            itemView.tag = "$name: $info"
        }

    }

    private class DetailsDiffCallback : DiffUtil.ItemCallback<DetailsProperty>() {
        override fun areItemsTheSame(oldItem: DetailsProperty, newItem: DetailsProperty) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DetailsProperty, newItem: DetailsProperty) = oldItem == newItem
    }

}