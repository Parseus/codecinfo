package com.parseus.codecinfo.ui.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.tracing.trace
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.databinding.DrmAdapterRowBinding
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.utils.copyToClipboard
import com.parseus.codecinfo.utils.getActivity
import com.parseus.codecinfo.utils.getColorOnSurfaceVariant
import com.parseus.codecinfo.utils.getHighlightedText
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.getSecondaryColor
import com.parseus.codecinfo.utils.getSelectedDrmInfoString
import com.parseus.codecinfo.utils.isInTwoPaneMode
import com.parseus.codecinfo.utils.setTextWithOptionalAutosizing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DrmAdapter : ListAdapter<DrmSimpleInfo, DrmAdapter.DrmInfoViewHolder>(DrmDiffCallback()) {

    private var currentSearchQuery: String? = null

    fun updateSearchQuery(query: String?) {
        currentSearchQuery = query
        notifyItemRangeChanged(0, itemCount, query)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrmInfoViewHolder {
        val binding = DrmAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val primaryColor = getPrimaryColor(parent.context)
        val secondaryColor = getSecondaryColor(parent.context)
        val onSurfaceVariantColor = getColorOnSurfaceVariant(parent.context)
        return DrmInfoViewHolder(binding, primaryColor, secondaryColor, onSurfaceVariantColor)
    }

    override fun onBindViewHolder(holder: DrmInfoViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val query = payloads[0] as? String
            holder.updateHighlighting(getItem(position), query ?: "")
        }
    }

    override fun onBindViewHolder(holder: DrmInfoViewHolder, position: Int) {
        holder.bindDrmInfo(getItem(position), position, currentSearchQuery)
    }

    inner class DrmInfoViewHolder(
        binding: DrmAdapterRowBinding,
        private val primaryColor: Int,
        private val secondaryColor: Int,
        private val onSurfaceVariantColor: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        private val layout = binding.simpleDrmRow
        private val drmId = binding.drmId
        private val drmName = binding.drmName
        private val moreInfo = binding.moreInfo

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
                    val drmSimpleInfo = getItem(position)
                    val context = layout.context
                    val activity = context.getActivity() as? MainActivity
                    activity?.let { act ->
                        // Do not create the same fragment again.
                        act.supportFragmentManager
                            .findFragmentByTag(act.getString(R.string.details_fragment_tag))?.let {
                                (it as DetailsFragment)
                                if (drmSimpleInfo.drmName == it.drmName && drmSimpleInfo.drmUuid == it.drmUuid) {
                                    return@setOnClickListener
                                }
                            }

                        val detailsFragment = DetailsFragment().also { fragment ->
                            fragment.arguments = Bundle().apply {
                                putString("drmName", drmSimpleInfo.drmName)
                                putSerializable("drmUuid", drmSimpleInfo.drmUuid)
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
                    val drmSimpleInfo = getItem(position)
                    val popup = PopupMenu(v.context, v)
                    popup.menu.add(Menu.NONE, 0, 0, R.string.context_menu_copy_drm_name)
                    popup.menu.add(Menu.NONE, 1, 1, R.string.context_menu_copy_drm_details)
                    popup.menu.add(Menu.NONE, 2, 2, R.string.action_share)

                    popup.setOnMenuItemClickListener { item ->
                        val activity = v.context.getActivity() as? MainActivity
                        when (item.itemId) {
                            0 -> {
                                v.context.copyToClipboard(v.context.getString(R.string.app_name), drmSimpleInfo.drmName, v)
                                true
                            }
                            1 -> {
                                activity?.lifecycleScope?.launch {
                                    val details = withContext(Dispatchers.IO) {
                                        getSelectedDrmInfoString(v.context, drmSimpleInfo.drmName, drmSimpleInfo.drmUuid)
                                    }
                                    v.context.copyToClipboard(v.context.getString(R.string.app_name), details, v)
                                }
                                true
                            }
                            2 -> {
                                activity?.lifecycleScope?.launch {
                                    val textToShare = withContext(Dispatchers.IO) {
                                        getSelectedDrmInfoString(v.context, drmSimpleInfo.drmName, drmSimpleInfo.drmUuid)
                                    }
                                    val title = "${v.context.getString(R.string.drm_details)}: ${drmSimpleInfo.drmName}"
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

        fun updateHighlighting(drmSimpleInfo: DrmSimpleInfo, query: String) = trace("DrmAdapter.updateHighlighting") {
            drmName.setTextWithOptionalAutosizing(
                text = getHighlightedText(drmSimpleInfo.drmName, query, primaryColor),
                minSize = 8, maxSize = 16, step = 1
            )
        }

        fun bindDrmInfo(drmSimpleInfo: DrmSimpleInfo, position: Int, query: String? = null) {
            if (query != null) {
                updateHighlighting(drmSimpleInfo, query)
            } else {
                drmName.setTextWithOptionalAutosizing(
                    text = drmSimpleInfo.drmName,
                    minSize = 8, maxSize = 16, step = 1
                )
            }
            drmId.setTextColor(primaryColor)
            drmName.setTextColor(secondaryColor)

            moreInfo.setTextColor(onSurfaceVariantColor)
            if (itemView.context.isInTwoPaneMode()) {
                moreInfo.visibility = View.GONE
            }
            layout.contentDescription = layout.context.getString(R.string.drm_row_content_description,
                    position, drmName.text)

            layout.tag = drmSimpleInfo.drmName
        }

    }

    private class DrmDiffCallback : DiffUtil.ItemCallback<DrmSimpleInfo>() {
        override fun areItemsTheSame(oldItem: DrmSimpleInfo, newItem: DrmSimpleInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DrmSimpleInfo, newItem: DrmSimpleInfo) = oldItem == newItem
    }

}