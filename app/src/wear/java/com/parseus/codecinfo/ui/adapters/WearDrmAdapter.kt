package com.parseus.codecinfo.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.databinding.WearDrmAdapterRowBinding
import com.parseus.codecinfo.ui.fragments.DetailsFragment

class WearDrmAdapter : ListAdapter<DrmSimpleInfo, WearDrmAdapter.ViewHolder>(DrmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WearDrmAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindDrmInfo(getItem(position))
    }

    inner class ViewHolder(private val binding: WearDrmAdapterRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val drmInfo = getItem(position)
                    val activity = itemView.context as? FragmentActivity
                    activity?.supportFragmentManager?.commit {
                        val detailsFragment = DetailsFragment().apply {
                            arguments = Bundle().apply {
                                putString("drmName", drmInfo.drmName)
                                putSerializable("drmUuid", drmInfo.drmUuid)
                            }
                        }
                        replace(android.R.id.content, detailsFragment)
                        addToBackStack(null)
                    }
                }
            }
        }

        fun bindDrmInfo(drmInfo: DrmSimpleInfo) {
            binding.drmName.text = drmInfo.drmName
        }
    }

    private class DrmDiffCallback : DiffUtil.ItemCallback<DrmSimpleInfo>() {
        override fun areItemsTheSame(oldItem: DrmSimpleInfo, newItem: DrmSimpleInfo) =
            oldItem.drmUuid == newItem.drmUuid
        override fun areContentsTheSame(oldItem: DrmSimpleInfo, newItem: DrmSimpleInfo) =
            oldItem == newItem
    }
}