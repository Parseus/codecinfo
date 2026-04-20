package com.parseus.codecinfo.ui.adapters

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.databinding.CodecAdapterRowBinding
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.utils.buildContainerTransform
import com.parseus.codecinfo.utils.getActivity
import com.parseus.codecinfo.utils.getColorOnSurfaceVariant
import com.parseus.codecinfo.utils.getHighlightedText
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.getSecondaryColor
import com.parseus.codecinfo.utils.isInTwoPaneMode

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

    class CodecInfoViewHolder(
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

        fun updateHighlighting(codecInfo: CodecSimpleInfo, query: String) {
            codecId.text = getHighlightedText(codecInfo.codecId, query, primaryColor)
            codecName.text = getHighlightedText(codecInfo.codecName, query, primaryColor)
        }

        fun bindCodecInfo(codecInfo: CodecSimpleInfo, position: Int, query: String? = null) {
            if (query != null) {
                updateHighlighting(codecInfo, query)
            } else {
                codecId.text = codecInfo.codecId
                codecName.text = codecInfo.codecName
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

            if (KNOWN_PROBLEMS_DB.isNotEmpty()) {
                val knownProblems = KNOWN_PROBLEMS_DB.any {
                    it.isAffected(itemView.context, codecInfo.codecName)
                }
                knownIssueIcon.isVisible = knownProblems
            }

            val codecTypeString = layout.context.getString(
                    if (codecInfo.isEncoder) R.string.encoder else R.string.decoder)
            val codecMediaTypeString = layout.context.getString(
                    if (codecInfo.isAudio) R.string.category_audio else R.string.category_video)

            layout.contentDescription = if (knownIssueIcon.isVisible) {
                layout.context.getString(R.string.codec_row_with_issue_content_description,
                        codecMediaTypeString, position, codecTypeString, codecName, codecId)
            } else {
                layout.context.getString(R.string.codec_row_content_description,
                        codecMediaTypeString, position, codecTypeString, codecName, codecId)
            }

            layout.transitionName = "$codecId/$codecName"
            layout.setOnClickListener {
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
                        if (!act.isInTwoPaneMode()) {
                            fragment.sharedElementEnterTransition = buildContainerTransform(layout, true)
                            fragment.sharedElementReturnTransition = buildContainerTransform(layout, false)
                        }
                    }

                    act.supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        if (act.isInTwoPaneMode()) {
                            replace(R.id.itemDetailsFragment, detailsFragment,
                                act.getString(R.string.details_fragment_tag))
                        } else {
                            addSharedElement(layout, layout.transitionName!!)
                            replace(R.id.content_fragment, detailsFragment,
                                act.getString(R.string.details_fragment_tag))
                            addToBackStack(null)
                        }
                    }
                    act.hideSearchView()
                }
            }
        }

    }

    private class CodecDiffCallback : DiffUtil.ItemCallback<CodecSimpleInfo>() {
        override fun areItemsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo) = oldItem == newItem
    }

}