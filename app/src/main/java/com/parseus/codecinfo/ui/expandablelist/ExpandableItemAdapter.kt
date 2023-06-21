package com.parseus.codecinfo.ui.expandablelist

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.util.Linkify.WEB_URLS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.databinding.ExpandableItemContentBinding
import com.parseus.codecinfo.databinding.ExpandableItemHeaderBinding
import com.parseus.codecinfo.ui.ImprovedBulletSpan
import kotlin.properties.Delegates

class ExpandableItemAdapter(private val knownProblemsList: List<KnownProblem>)
    : RecyclerView.Adapter<ExpandableItemAdapter.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_HEADER = 2

        private const val EXPANDED_ROTATION_DEG = 0f
        private const val COLLAPSED_ROTATION_DEG = 180f
    }

    private var isExpanded: Boolean by Delegates.observable(true) { _, _: Boolean, newExpandedValue: Boolean ->
        if (newExpandedValue) {
            notifyItemRangeInserted(1, knownProblemsList.size)
        } else {
            notifyItemRangeRemoved(1, knownProblemsList.size)
        }

        // to update the header expand icon
        notifyItemChanged(0)
    }

    private val headerClickListener = View.OnClickListener {
        isExpanded = !isExpanded
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return if (isExpanded) knownProblemsList.size + 1 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_HEADER -> ViewHolder.HeaderViewHolder(
                    ExpandableItemHeaderBinding.inflate(inflater, parent, false))
            else -> ViewHolder.ItemViewHolder(ExpandableItemContentBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.HeaderViewHolder -> holder.bind(isExpanded, headerClickListener)
            is ViewHolder.ItemViewHolder -> holder.bind(knownProblemsList[position - 1], position)
        }
    }

    sealed class ViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        class ItemViewHolder(private val binding: ExpandableItemContentBinding) : ViewHolder(binding) {

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

        class HeaderViewHolder(private val binding: ExpandableItemHeaderBinding) : ViewHolder(binding) {

            val expandIcon = binding.expandIcon

            fun bind(expanded: Boolean, clickListener: View.OnClickListener) {
                binding.expandIcon.rotation = if (expanded) EXPANDED_ROTATION_DEG else COLLAPSED_ROTATION_DEG
                itemView.setOnClickListener(clickListener)
            }

        }

    }

}