package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.WearMoreAdapterRowBinding

class WearShareActionAdapter(private val onShareClicked: () -> Unit) : RecyclerView.Adapter<WearShareActionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WearMoreAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.actionTitle.setText(R.string.action_share)
        holder.binding.actionIcon.setImageResource(R.drawable.ic_share)
        holder.itemView.setOnClickListener { onShareClicked() }
    }

    override fun getItemCount() = 1

    class ViewHolder(val binding: WearMoreAdapterRowBinding) : RecyclerView.ViewHolder(binding.root)
}