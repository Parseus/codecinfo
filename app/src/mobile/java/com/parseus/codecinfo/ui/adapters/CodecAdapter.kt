package com.parseus.codecinfo.ui.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.tracing.trace
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.databinding.CodecAdapterRowBinding
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.utils.copyToClipboard
import com.parseus.codecinfo.utils.getActivity
import com.parseus.codecinfo.utils.getColorOnSurfaceVariant
import com.parseus.codecinfo.utils.getHighlightedText
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.getSecondaryColor
import com.parseus.codecinfo.utils.getSelectedCodecInfoString
import com.parseus.codecinfo.utils.isInTwoPaneMode
import com.parseus.codecinfo.utils.setTextWithOptionalAutosizing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CodecAdapter : ListAdapter<CodecSimpleInfo, CodecAdapter.CodecInfoViewHolder>(
    CodecDiffCallback()
) {

    private var currentSearchQuery: String? = null

    fun updateSearchQuery(query: String?) {
        currentSearchQuery = query
        notifyItemRangeChanged(0, itemCount, query)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodecInfoViewHolder {
        val binding = CodecAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val primaryColor = getPrimaryColor(parent.context)
        val secondaryColor = getSecondaryColor(parent.context)
        val onSurfaceVariantColor = getColorOnSurfaceVariant(parent.context)
        return CodecInfoViewHolder(binding, primaryColor, secondaryColor, onSurfaceVariantColor)
    }

    override fun onBindViewHolder(holder: CodecInfoViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val query = payloads[0] as? String
            holder.updateHighlighting(getItem(position), query ?: "")
        }
    }

    override fun onBindViewHolder(holder: CodecInfoViewHolder, position: Int) {
        holder.bindCodecInfo(getItem(position), position, currentSearchQuery)
    }

    inner class CodecInfoViewHolder(
        binding: CodecAdapterRowBinding,
        private val primaryColor: Int,
        private val secondaryColor: Int,
        private val onSurfaceVariantColor: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        private val layout = binding.simpleCodecRow
        private val knownIssueIcon = binding.knownProblemIcon
        private val codecId = binding.codecName
        private val codecName = binding.codecFullName
        private val codecType = binding.codecType
        private val moreInfo = binding.moreInfo
        private val hwIcon = binding.hwIcon

        init {
            if (Build.VERSION.SDK_INT >= 24) {
                layout.pointerIcon = PointerIcon.getSystemIcon(layout.context, PointerIcon.TYPE_HAND)

                layout.setOnLongClickListener { v ->
                    val textToDrag = v.tag as? String ?: return@setOnLongClickListener false
                    val item = ClipData.Item(textToDrag)
                    val dragData = ClipData(textToDrag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                    v.startDragAndDrop(dragData, View.DragShadowBuilder(v), null, View.DRAG_FLAG_GLOBAL)
                }
            }

            layout.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val codecInfo = getItem(position)
                    val context = layout.context
                    val activity = context.getActivity() as? MainActivity
                    activity?.let { act ->
                        // Do not create the same fragment again.
                        act.supportFragmentManager
                            .findFragmentByTag(act.getString(R.string.details_fragment_tag))?.let {
                                (it as DetailsFragment)
                                if (codecInfo.codecId == it.codecId && codecInfo.codecName == it.codecName) {
                                    return@setOnClickListener
                                }
                            }

                        val detailsFragment = DetailsFragment().also { fragment ->
                            fragment.arguments = Bundle().apply {
                                putString("codecId", codecInfo.codecId)
                                putString("codecName", codecInfo.codecName)
                            }
                        }

                        act.supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            if (act.isInTwoPaneMode()) {
                                replace(R.id.itemDetailsFragment, detailsFragment,
                                    act.getString(R.string.details_fragment_tag))
                            } else {
                                replace(R.id.content_fragment, detailsFragment,
                                    act.getString(R.string.details_fragment_tag))
                                addToBackStack(null)
                            }
                        }
                        act.hideSearchView()
                    }
                }
            }

            layout.setOnContextClickListener { v ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val codecInfo = getItem(position)
                    val popup = PopupMenu(v.context, v)
                    popup.menu.add(Menu.NONE, 0, 0, R.string.context_menu_copy_codec_name)
                    popup.menu.add(Menu.NONE, 1, 1, R.string.context_menu_copy_codec_details)
                    popup.menu.add(Menu.NONE, 2, 2, R.string.action_share)

                    popup.setOnMenuItemClickListener { item ->
                        val activity = v.context.getActivity() as? MainActivity
                        when (item.itemId) {
                            0 -> {
                                v.context.copyToClipboard(v.context.getString(R.string.app_name), codecInfo.codecName, v)
                                true
                            }
                            1 -> {
                                activity?.lifecycleScope?.launch {
                                    val details = withContext(Dispatchers.IO) {
                                        getSelectedCodecInfoString(v.context, codecInfo.codecId, codecInfo.codecName)
                                    }
                                    v.context.copyToClipboard(v.context.getString(R.string.app_name), details, v)
                                }
                                true
                            }
                            2 -> {
                                activity?.lifecycleScope?.launch {
                                    val textToShare = withContext(Dispatchers.IO) {
                                        getSelectedCodecInfoString(v.context, codecInfo.codecId, codecInfo.codecName)
                                    }
                                    val title = "${v.context.getString(R.string.codec_details)}: ${codecInfo.codecName}"
                                    activity.shareSingleItem(textToShare, title)
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
                true
            }
        }

        fun updateHighlighting(codecInfo: CodecSimpleInfo, query: String) = trace("CodecAdapter.updateHighlighting") {
            codecId.text = getHighlightedText(codecInfo.codecId, query, primaryColor)
            codecName.setTextWithOptionalAutosizing(
                text = getHighlightedText(codecInfo.codecName, query, primaryColor),
                minSize = 8, maxSize = 16, step = 1
            )
        }

        fun bindCodecInfo(codecInfo: CodecSimpleInfo, position: Int, query: String? = null) {
            if (query != null) {
                updateHighlighting(codecInfo, query)
            } else {
                codecId.text = codecInfo.codecId
                codecName.setTextWithOptionalAutosizing(
                    text = codecInfo.codecName,
                    minSize = 8, maxSize = 16, step = 1
                )
            }
            codecId.setTextColor(primaryColor)
            codecName.setTextColor(secondaryColor)

            codecType.text = itemView.resources.getString(
                    if (codecInfo.isEncoder) R.string.encoder else R.string.decoder)
            codecType.setTextColor(onSurfaceVariantColor)
            moreInfo.setTextColor(onSurfaceVariantColor)
            if (itemView.context.isInTwoPaneMode()) {
                moreInfo.visibility = View.GONE
            }

            hwIcon.isVisible = codecInfo.isHardwareAccelereated
                    && itemView.context.settingsRepository.getSettingsSync().showHwIcon
            hwIcon.imageTintList = ColorStateList.valueOf(onSurfaceVariantColor)

            knownIssueIcon.isVisible = codecInfo.hasKnownProblem

            val codecTypeString = layout.context.getString(
                    if (codecInfo.isEncoder) R.string.encoder else R.string.decoder)
            val codecMediaTypeString = layout.context.getString(
                    if (codecInfo.isAudio) R.string.category_audio else R.string.category_video)

            layout.contentDescription = when {
                hwIcon.isVisible && knownIssueIcon.isVisible -> {
                    layout.context.getString(R.string.codec_row_hw_accelerated_with_issue_content_description,
                        codecMediaTypeString, position, codecTypeString, codecName.text, codecId.text)
                }
                hwIcon.isVisible -> {
                    layout.context.getString(R.string.codec_row_hw_accelerated_content_description,
                        codecMediaTypeString, position, codecTypeString, codecName.text, codecId.text)
                }
                knownIssueIcon.isVisible -> {
                    layout.context.getString(R.string.codec_row_with_issue_content_description,
                        codecMediaTypeString, position, codecTypeString, codecName.text, codecId.text)
                }
                else -> {
                    layout.context.getString(R.string.codec_row_content_description,
                        codecMediaTypeString, position, codecTypeString, codecName.text, codecId.text)
                }
            }

            layout.tag = codecInfo.codecName
        }

    }

    private class CodecDiffCallback : DiffUtil.ItemCallback<CodecSimpleInfo>() {
        override fun areItemsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo) = oldItem == newItem
    }

}