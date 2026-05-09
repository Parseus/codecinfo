package com.parseus.codecinfo.ui.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.Menu
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.HtmlCompat
import androidx.core.text.PrecomputedTextCompat
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.databinding.ExpandableItemContentBinding
import com.parseus.codecinfo.databinding.ExpandableItemHeaderBinding
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding
import com.parseus.codecinfo.ui.ImprovedBulletSpan
import com.parseus.codecinfo.utils.copyToClipboard
import com.parseus.codecinfo.utils.getAttributeResourceId

open class DetailsAdapter(private val onHeaderClick: (Int) -> Unit = {}) : ListAdapter<DetailItem, RecyclerView.ViewHolder>(
    DetailsDiffCallback()
) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_KNOWN_PROBLEM = 1
        const val TYPE_PROPERTY = 2
        const val TYPE_PROPERTY_BLOCK = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DetailItem.Header -> TYPE_HEADER
            is DetailItem.KnownProblemItem -> TYPE_KNOWN_PROBLEM
            is DetailItem.PropertyItem -> TYPE_PROPERTY
            is DetailItem.PropertyBlock -> TYPE_PROPERTY_BLOCK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(ExpandableItemHeaderBinding.inflate(inflater, parent, false), onHeaderClick)
            TYPE_KNOWN_PROBLEM -> KnownProblemViewHolder(ExpandableItemContentBinding.inflate(inflater, parent, false))
            TYPE_PROPERTY -> DetailsViewHolder(ItemDetailsAdapterRowBinding.inflate(inflater, parent, false))
            TYPE_PROPERTY_BLOCK -> PropertyBlockViewHolder(ItemDetailsAdapterRowBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = item is DetailItem.Header || item is DetailItem.KnownProblemItem
        }

        when (holder) {
            is HeaderViewHolder -> holder.bind((item as DetailItem.Header).isExpanded)
            is KnownProblemViewHolder -> holder.bind((item as DetailItem.KnownProblemItem).problem)
            is DetailsViewHolder -> {
                if (holder is PropertyBlockViewHolder) {
                    val properties = (item as DetailItem.PropertyBlock).properties
                    holder.bindBlock(properties)
                } else {
                    val property = (item as DetailItem.PropertyItem).property
                    val nextItem = if (position + 1 < itemCount) getItem(position + 1) else null
                    val isFollowedByContinuation = nextItem is DetailItem.PropertyItem && nextItem.property.name.isEmpty()
                    holder.bindDetails(property.name, property.value, isFollowedByContinuation)
                }
            }
        }
    }

    class HeaderViewHolder(private val binding: ExpandableItemHeaderBinding, private val onHeaderClick: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        val expandIcon = binding.expandIcon

        init {
            if (Build.VERSION.SDK_INT >= 24) {
                itemView.pointerIcon = PointerIcon.getSystemIcon(itemView.context, PointerIcon.TYPE_HAND)
            }
        }

        fun bind(isExpanded: Boolean) {
            binding.expandIcon.rotation = if (isExpanded) 0f else 180f
            itemView.setOnClickListener { onHeaderClick(bindingAdapterPosition) }
            ViewCompat.setAccessibilityHeading(itemView, true)
            ViewCompat.setStateDescription(itemView,
                itemView.context.getString(if (isExpanded)
                    R.string.state_expanded else R.string.state_collapsed))
        }
    }

    class KnownProblemViewHolder(private val binding: ExpandableItemContentBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.knownIssueItemSources.movementMethod = LinkMovementMethodCompat.getInstance()
        }

        fun bind(knownProblem: KnownProblem) {
            val text = HtmlCompat.fromHtml(knownProblem.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
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

        protected val codecProperty = binding.codecProperty
        protected val codecValue = binding.codecValue as AppCompatTextView

        protected var currentName = ""
        protected var currentInfo = ""

        init {
            if (Build.VERSION.SDK_INT >= 24) {
                itemView.setOnLongClickListener { v ->
                    val textToDrag = "$currentName: $currentInfo"
                    val item = ClipData.Item(textToDrag)
                    val dragData = ClipData(textToDrag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                    v.startDragAndDrop(dragData, View.DragShadowBuilder(v), null, View.DRAG_FLAG_GLOBAL)
                }

                codecValue.setOnLongClickListener { v ->
                    val textToDrag = currentInfo
                    val item = ClipData.Item(textToDrag)
                    val dragData = ClipData(textToDrag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                    v.startDragAndDrop(dragData, View.DragShadowBuilder(v), null, View.DRAG_FLAG_GLOBAL)
                    true
                }

                itemView.pointerIcon = PointerIcon.getSystemIcon(itemView.context, PointerIcon.TYPE_HAND)
            }

            itemView.setOnContextClickListener { v ->
                val popup = PopupMenu(v.context, v)
                popup.menu.add(Menu.NONE, 0, 0, R.string.context_menu_copy_property_value)
                popup.menu.add(Menu.NONE, 1, 1, R.string.context_menu_copy_property_full)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        0 -> {
                            v.context.copyToClipboard(v.context.getString(R.string.app_name), currentInfo, v)
                            true
                        }
                        1 -> {
                            v.context.copyToClipboard(v.context.getString(R.string.app_name), "$currentName: $currentInfo", v)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
                true
            }

            ViewCompat.setAccessibilityDelegate(itemView, object : androidx.core.view.AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        0, host.context.getString(R.string.context_menu_copy_property_value)))
                    info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        1, host.context.getString(R.string.context_menu_copy_property_full)))
                }

                override fun performAccessibilityAction(host: View, action: Int, args: android.os.Bundle?): Boolean {
                    return when (action) {
                        0 -> {
                            host.context.copyToClipboard(host.context.getString(R.string.app_name), currentInfo, host)
                            true
                        }
                        1 -> {
                            host.context.copyToClipboard(host.context.getString(R.string.app_name), "$currentName: $currentInfo", host)
                            true
                        }
                        else -> super.performAccessibilityAction(host, action, args)
                    }
                }
            })
        }

        @CallSuper
        open fun bindDetails(name: String, info: String, isMultiLineProperty: Boolean) {
            val resources = itemView.resources

            currentName = name
            currentInfo = info

            if (name.isEmpty()) {
                codecProperty.isVisible = false
            } else {
                codecProperty.isVisible = true
                codecProperty.text = name
            }

            val bottomPadding = if (isMultiLineProperty) 0 else resources.getDimensionPixelSize(R.dimen.details_row_bottom_padding)
            itemView.updatePadding(bottom = bottomPadding)
            codecValue.updateLayoutParams<LinearLayout.LayoutParams> {
                topMargin = if (name.isEmpty()) {
                    -resources.getDimensionPixelSize(R.dimen.details_row_continuation_margin)
                } else {
                    0
                }
            }

            codecValue.setTextFuture(
                PrecomputedTextCompat.getTextFuture(info,
                    codecValue.textMetricsParamsCompat, null)
            )

            itemView.tag = "$name: $info"
            itemView.contentDescription = "$name: $info"
        }

    }

    class PropertyBlockViewHolder(binding: ItemDetailsAdapterRowBinding) : DetailsViewHolder(binding) {
        private val container = itemView as ViewGroup
        private val valuePool = mutableListOf<AppCompatTextView>()

        fun bindBlock(properties: List<DetailsProperty>) {
            val resources = itemView.resources
            val mainProperty = properties.first()
            
            bindDetails(mainProperty.name, mainProperty.value, properties.size > 1)

            val continuations = properties.drop(1)
            
            for (i in continuations.indices) {
                val prop = continuations[i]
                val textView = if (i < valuePool.size) {
                    valuePool[i].apply { isVisible = true }
                } else {
                    createValueTextView().also { 
                        valuePool.add(it)
                        container.addView(it)
                    }
                }

                textView.updateLayoutParams<LinearLayout.LayoutParams> {
                    topMargin = -resources.getDimensionPixelSize(R.dimen.details_row_continuation_margin)
                }
                textView.tag = prop.value
                textView.setTextFuture(PrecomputedTextCompat.getTextFuture(
                    prop.value, textView.textMetricsParamsCompat, null
                ))
            }

            for (i in continuations.size until valuePool.size) {
                valuePool[i].isVisible = false
            }

            itemView.tag = properties.joinToString("\n") { "${it.name}: ${it.value}" }
            itemView.contentDescription = itemView.tag as String
        }

        @SuppressLint("DiscouragedApi")
        private fun createValueTextView(): AppCompatTextView {
            return AppCompatTextView(itemView.context).apply {
                id = View.generateViewId()
                val textAppearanceAttr = context.resources.getIdentifier("textAppearanceBodyMedium", "attr", context.packageName)
                val finalAttr = if (textAppearanceAttr != 0) textAppearanceAttr else androidx.appcompat.R.attr.textAppearanceListItemSecondary
                setTextAppearance(context.getAttributeResourceId(finalAttr))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                if (Build.VERSION.SDK_INT >= 24) {
                    setOnLongClickListener { v ->
                        val textToDrag = v.tag as? String ?: return@setOnLongClickListener false
                        val item = ClipData.Item(textToDrag)
                        val dragData = ClipData(textToDrag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                        v.startDragAndDrop(dragData, View.DragShadowBuilder(v), null, View.DRAG_FLAG_GLOBAL)
                        true
                    }
                }
            }
        }
    }

    private class DetailsDiffCallback : DiffUtil.ItemCallback<DetailItem>() {
        override fun areItemsTheSame(oldItem: DetailItem, newItem: DetailItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DetailItem, newItem: DetailItem) = oldItem == newItem
    }

}