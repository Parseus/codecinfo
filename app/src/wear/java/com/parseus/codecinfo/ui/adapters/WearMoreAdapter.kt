package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.databinding.WearMoreAdapterRowBinding

class WearMoreAdapter(private val onItemClicked: (MoreAction) -> Unit) :
    ListAdapter<MoreAction, WearMoreAdapter.ViewHolder>(MoreActionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WearMoreAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = getItem(position)
        holder.bindAction(action)
    }

    inner class ViewHolder(binding: WearMoreAdapterRowBinding, private val onItemClicked: (MoreAction) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        private val actionTitle = binding.actionTitle
        private val actionIcon = binding.actionIcon

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val action = getItem(position)
                    onItemClicked(action)
                }
            }
        }

        fun bindAction(action: MoreAction) {
            actionTitle.setText(action.titleResId)
            actionIcon.setImageResource(action.iconResId)
        }

    }

    private class MoreActionDiffCallback : DiffUtil.ItemCallback<MoreAction>() {
        override fun areItemsTheSame(oldItem: MoreAction, newItem: MoreAction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MoreAction, newItem: MoreAction) = oldItem == newItem
    }
}