package com.parseus.codecinfo.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.databinding.WearSearchHeaderRowBinding
import com.parseus.codecinfo.ui.WearSearchActivity

class WearSearchHeaderAdapter : RecyclerView.Adapter<WearSearchHeaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WearSearchHeaderRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, WearSearchActivity::class.java)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = 1

    class ViewHolder(binding: WearSearchHeaderRowBinding)
        : RecyclerView.ViewHolder(binding.root)

}