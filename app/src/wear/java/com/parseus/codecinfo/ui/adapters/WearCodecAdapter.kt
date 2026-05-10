package com.parseus.codecinfo.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.databinding.WearCodecAdapterRowBinding
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.utils.setTextWithOptionalAutosizing

class WearCodecAdapter: ListAdapter<CodecSimpleInfo, WearCodecAdapter.ViewHolder>(
    CodecDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WearCodecAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindCodecInfo(getItem(position), position)
    }

    inner class ViewHolder(binding: WearCodecAdapterRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val codecId = binding.codecId
        private val codecName = binding.codecName

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val codecInfo = getItem(position)
                    val activity = itemView.context as? FragmentActivity
                    activity?.supportFragmentManager?.commit {
                        val detailsFragment = DetailsFragment().apply {
                            arguments = Bundle().apply {
                                putString("codecId", codecInfo.codecId)
                                putString("codecName", codecInfo.codecName)
                            }
                        }
                        replace(android.R.id.content, detailsFragment)
                        addToBackStack(null)
                    }
                }
            }
        }

        fun bindCodecInfo(codecInfo: CodecSimpleInfo, position: Int) {
            codecId.text = codecInfo.codecId
            codecName.setTextWithOptionalAutosizing(
                text = codecInfo.codecName,
                minSize = 8, maxSize = 16, step = 1
            )

            val context = itemView.context
            val codecTypeString = context.getString(
                if (codecInfo.isEncoder) R.string.encoder else R.string.decoder
            )
            val codecMediaTypeString = context.getString(
                if (codecInfo.isAudio) R.string.category_audio else R.string.category_video
            )

            itemView.contentDescription = when {
                codecInfo.isHardwareAccelereated && codecInfo.hasKnownProblem -> {
                    context.getString(R.string.codec_row_hw_accelerated_with_issue_content_description,
                        codecMediaTypeString, position, codecTypeString, codecInfo.codecName, codecInfo.codecId)
                }
                codecInfo.isHardwareAccelereated -> {
                    context.getString(R.string.codec_row_hw_accelerated_content_description,
                        codecMediaTypeString, position, codecTypeString, codecInfo.codecName, codecInfo.codecId)
                }
                codecInfo.hasKnownProblem -> {
                    context.getString(R.string.codec_row_with_issue_content_description,
                        codecMediaTypeString, position, codecTypeString, codecInfo.codecName, codecInfo.codecId)
                }
                else -> {
                    context.getString(R.string.codec_row_content_description,
                        codecMediaTypeString, position, codecTypeString, codecInfo.codecName, codecInfo.codecId)
                }
            }
        }

    }

    private class CodecDiffCallback : DiffUtil.ItemCallback<CodecSimpleInfo>() {
        override fun areItemsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo) = oldItem == newItem
    }

}