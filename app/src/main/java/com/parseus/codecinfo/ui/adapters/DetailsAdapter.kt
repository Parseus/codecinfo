package com.parseus.codecinfo.ui.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.PointerIcon
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import androidx.core.text.PrecomputedTextCompat
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.databinding.ExpandableItemContentBinding
import com.parseus.codecinfo.databinding.ExpandableItemHeaderBinding
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding
import com.parseus.codecinfo.ui.ImprovedBulletSpan

open class DetailsAdapter(private val onHeaderClick: (Int) -> Unit = {}) : ListAdapter<DetailItem, RecyclerView.ViewHolder>(
    DetailsDiffCallback()
) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_KNOWN_PROBLEM = 1
        const val TYPE_PROPERTY = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DetailItem.Header -> TYPE_HEADER
            is DetailItem.KnownProblemItem -> TYPE_KNOWN_PROBLEM
            is DetailItem.PropertyItem -> TYPE_PROPERTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(ExpandableItemHeaderBinding.inflate(inflater, parent, false), onHeaderClick)
            TYPE_KNOWN_PROBLEM -> KnownProblemViewHolder(ExpandableItemContentBinding.inflate(inflater, parent, false))
            TYPE_PROPERTY -> DetailsViewHolder(ItemDetailsAdapterRowBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind((item as DetailItem.Header).isExpanded)
            is KnownProblemViewHolder -> holder.bind((item as DetailItem.KnownProblemItem).problem, position)
            is DetailsViewHolder -> {
                val property = (item as DetailItem.PropertyItem).property
                val nextItem = if (position + 1 < itemCount) getItem(position + 1) else null
                val isFollowedByContinuation = nextItem is DetailItem.PropertyItem && nextItem.property.name.isEmpty()
                holder.bindDetails(property.name, property.value, isFollowedByContinuation)
            }
        }
    }

    class HeaderViewHolder(private val binding: ExpandableItemHeaderBinding, private val onHeaderClick: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        val expandIcon = binding.expandIcon

        fun bind(isExpanded: Boolean) {
            binding.expandIcon.rotation = if (isExpanded) 0f else 180f
            itemView.setOnClickListener { onHeaderClick(bindingAdapterPosition) }
        }
    }

    class KnownProblemViewHolder(private val binding: ExpandableItemContentBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.knownIssueItemSources.movementMethod = LinkMovementMethodCompat.getInstance()
        }

        fun bind(knownProblem: KnownProblem, position: Int) {
            val text = HtmlCompat.fromHtml(knownProblem.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.root.contentDescription = binding.root.context.getString(
                R.string.known_issue_content_description, position, text,
                knownProblem.urls.joinToString())
            binding.knownIssueItemDesc.text = text
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
                LinkifyCompat.addLinks(this, Linkify.WEB_URLS)
            }
        }
    }

    open class DetailsViewHolder(binding: ItemDetailsAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        protected val codecName = binding.codecProperty
        protected val codecInfo = binding.codecValue as AppCompatTextView

        @CallSuper
        open fun bindDetails(name: String, info: String, isMultiLineProperty: Boolean) {
            val resources = itemView.resources

            if (name.isEmpty()) {
                codecName.isVisible = false
            } else {
                codecName.isVisible = true
                codecName.text = name
            }

            val bottomPadding = if (isMultiLineProperty) 0 else resources.getDimensionPixelSize(R.dimen.details_row_bottom_padding)
            itemView.updatePadding(bottom = bottomPadding)
            codecInfo.updateLayoutParams<LinearLayout.LayoutParams> {
                topMargin = if (name.isEmpty()) {
                    -resources.getDimensionPixelSize(R.dimen.details_row_continuation_margin)
                } else {
                    0
                }
            }

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
            itemView.contentDescription = "$name: $info"
        }

    }

    private class DetailsDiffCallback : DiffUtil.ItemCallback<DetailItem>() {
        override fun areItemsTheSame(oldItem: DetailItem, newItem: DetailItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DetailItem, newItem: DetailItem) = oldItem == newItem
    }

}
