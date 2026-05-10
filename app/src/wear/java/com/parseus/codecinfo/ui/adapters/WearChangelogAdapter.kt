package com.parseus.codecinfo.ui.adapters

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.WearChangelogAdapterRowBinding
import com.parseus.codecinfo.ui.ImprovedBulletSpan

sealed class ChangelogItem {
    data class Version(val version: String) : ChangelogItem()
    data class Change(val change: String) : ChangelogItem()
}

class WearChangelogAdapter : ListAdapter<ChangelogItem, WearChangelogAdapter.ViewHolder>(
    ChangelogDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WearChangelogAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(binding: WearChangelogAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val versionTitle = binding.versionTitle
        private val versionChanges = binding.versionChanges

        fun bind(item: ChangelogItem) {
            when (item) {
                is ChangelogItem.Version -> {
                    versionTitle.isVisible = true
                    versionChanges.isVisible = false

                    versionTitle.text = versionTitle.context.getString(
                        R.string.app_version, item.version
                    )
                }
                is ChangelogItem.Change -> {
                    versionTitle.isVisible = false
                    versionChanges.isVisible = true

                    val spannableBuilder = SpannableStringBuilder(item.change)
                    spannableBuilder.setSpan(
                        ImprovedBulletSpan(),
                        0,
                        item.change.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    versionChanges.text = spannableBuilder
                }
            }
        }

    }

    private class ChangelogDiffCallback : DiffUtil.ItemCallback<ChangelogItem>() {
        override fun areItemsTheSame(oldItem: ChangelogItem, newItem: ChangelogItem) = oldItem == newItem
        override fun areContentsTheSame(oldItem: ChangelogItem, newItem: ChangelogItem) = oldItem == newItem
    }

}