package com.parseus.codecinfo.ui.adapters

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.util.Linkify.WEB_URLS
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.databinding.ExpandableItemContentBinding
import com.parseus.codecinfo.ui.ImprovedBulletSpan

class DeviceIssuesAdapter(private val deviceIssuesList: List<KnownProblem>)
    : RecyclerView.Adapter<DeviceIssuesAdapter.ItemViewHolder>() {

    override fun getItemViewType(position: Int) = position
    override fun getItemCount(): Int = deviceIssuesList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(ExpandableItemContentBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(deviceIssuesList[position], position)
    }

    class ItemViewHolder(private val binding: ExpandableItemContentBinding) : ViewHolder(binding.root) {

        fun bind(knownProblem: KnownProblem, position: Int) {
            binding.root.contentDescription = binding.root.context.getString(
                R.string.known_issue_content_description, position, knownProblem.description,
                knownProblem.urls.joinToString())
            binding.knownIssueItemDesc.text = knownProblem.description
            val spannableBuilder = SpannableStringBuilder()
            with (spannableBuilder) {
                knownProblem.urls.forEach {
                    val start = length
                    append(it)
                    setSpan(ImprovedBulletSpan(), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            binding.knownIssueItemSources.run {
                setText(spannableBuilder, TextView.BufferType.SPANNABLE)
                LinkifyCompat.addLinks(this, WEB_URLS)
                movementMethod = LinkMovementMethodCompat.getInstance()
            }
        }
    }

}