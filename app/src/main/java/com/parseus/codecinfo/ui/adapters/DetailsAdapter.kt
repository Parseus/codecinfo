package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding

open class DetailsAdapter : RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder>() {

    private val sortedList = SortedList(DetailsProperty::class.java, object : SortedListAdapterCallback<DetailsProperty>(this) {
        override fun compare(o1: DetailsProperty, o2: DetailsProperty): Int {
            return o1.id.compareTo(o2.id)
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun areContentsTheSame(oldItem: DetailsProperty, newItem: DetailsProperty): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(item1: DetailsProperty, item2: DetailsProperty): Boolean {
            return item1.id == item2.id
        }

    })

    fun add(infoList: List<DetailsProperty>) {
        sortedList.addAll(infoList)
    }

    fun replaceAll(infoList: List<DetailsProperty>) {
        sortedList.run {
            beginBatchedUpdates()
            for (i in sortedList.size() - 1 downTo 0) {
                val info = sortedList[i]
                if (!infoList.contains(info)) {
                    sortedList.remove(info)
                }
            }
            addAll(infoList)
            endBatchedUpdates()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val binding = ItemDetailsAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val property = sortedList[position]
        val name = property.name
        val info = property.value
        holder.bindDetails(name, info)
    }

    override fun getItemCount(): Int = sortedList.size()

    open class DetailsViewHolder(binding: ItemDetailsAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        protected val codecName = binding.codecProperty
        protected val codecInfo = binding.codecValue

        @CallSuper
        open fun bindDetails(name: String, info: String) {
            codecName.text = name
            codecInfo.text = info
        }

    }
}