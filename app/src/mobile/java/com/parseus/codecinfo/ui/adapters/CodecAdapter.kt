package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.databinding.CodecAdapterRowBinding
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.utils.*

class CodecAdapter : RecyclerView.Adapter<CodecAdapter.CodecInfoViewHolder>() {

    private val sortedList = SortedList(CodecSimpleInfo::class.java, object : SortedListAdapterCallback<CodecSimpleInfo>(this) {
        override fun compare(o1: CodecSimpleInfo, o2: CodecSimpleInfo): Int {
            val comp = o1.codecId.compareTo(o2.codecId)
            return if (comp != 0) comp else o1.codecName.compareTo(o2.codecName)
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun areContentsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(item1: CodecSimpleInfo, item2: CodecSimpleInfo): Boolean {
            return item1.id == item2.id
        }

    })

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return sortedList.get(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun add(infoList: List<CodecSimpleInfo>) {
        sortedList.addAll(infoList)
    }

    fun replaceAll(infoList: List<CodecSimpleInfo>) {
        sortedList.run {
            beginBatchedUpdates()
            for (i in sortedList.size() - 1 downTo 0) {
                val info = sortedList[i]
                if (!infoList.contains(info)) {
                    sortedList.remove(info)
                }
            }
            addAll(infoList)
            endBatchedUpdates()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodecInfoViewHolder {
        val binding = CodecAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CodecInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CodecInfoViewHolder, position: Int) {
        val codecInfoItem = sortedList[position]
        holder.bindCodecInfo(codecInfoItem, position)
    }

    override fun getItemCount() = sortedList.size()

    class CodecInfoViewHolder(binding: CodecAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val layout = binding.simpleCodecRow
        private val knownIssueIcon = binding.knownProblemIcon
        private val codecId = binding.codecName
        private val codecName = binding.codecFullName
        private val codecType = binding.codecType
        private val moreInfo = binding.moreInfo
        private val hwIcon = binding.hwIcon

        fun bindCodecInfo(codecInfo: CodecSimpleInfo, position: Int) {
            codecId.text = codecInfo.codecId
            codecId.setTextColor(getPrimaryColor(codecId.context))
            codecName.text = codecInfo.codecName
            codecName.setTextColor(getSecondaryColor(codecName.context))

            codecType.text = itemView.resources.getString(
                    if (codecInfo.isEncoder) R.string.encoder else R.string.decoder)
            if (itemView.context.isInTwoPaneMode()) {
                moreInfo.visibility = View.GONE
            }

            hwIcon.isVisible = codecInfo.isHardwareAccelereated
                    && PreferenceManager.getDefaultSharedPreferences(layout.context).getBoolean("show_hw_icon", true)

            if (KNOWN_PROBLEMS_DB.isNotEmpty()) {
                val knownProblems = KNOWN_PROBLEMS_DB.filter {
                    it.isAffected(itemView.context, codecInfo.codecName)
                }
                if (knownProblems.isNotEmpty()) {
                    knownIssueIcon.isVisible = true
                }
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

            ViewCompat.setTransitionName(layout, "$codecId/$codecName")
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
                        fragment.arguments = bundleOf(
                            "codecId" to codecInfo.codecId,
                            "codecName" to codecInfo.codecName
                        )
                        if (!act.isInTwoPaneMode()) {
                            fragment.sharedElementEnterTransition = buildContainerTransform(layout, true)
                            fragment.sharedElementReturnTransition = buildContainerTransform(layout, false)
                        }
                        fragment.searchListenerDestroyedListener = object : SearchListenerDestroyedListener {
                            override fun onSearchListenerDestroyed(queryTextListener: SearchView.OnQueryTextListener) {
                                act.searchListeners.remove(queryTextListener)
                            }
                        }
                    }

                    val existingFragment = act.searchListeners.find { it is DetailsFragment }
                    existingFragment?.let { act.searchListeners.remove(it) }
                    act.searchListeners.add(detailsFragment)

                    act.supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        if (act.isInTwoPaneMode()) {
                            replace(R.id.itemDetailsFragment, detailsFragment,
                                act.getString(R.string.details_fragment_tag))
                        } else {
                            addSharedElement(layout, ViewCompat.getTransitionName(layout)!!)
                            replace(R.id.content_fragment, detailsFragment,
                                act.getString(R.string.details_fragment_tag))
                            addToBackStack(null)

                            act.supportActionBar!!.apply {
                                setDisplayHomeAsUpEnabled(true)
                                setHomeButtonEnabled(true)
                                setHomeActionContentDescription(R.string.close_details)
                            }
                        }
                    }
                }
            }
        }

    }

}