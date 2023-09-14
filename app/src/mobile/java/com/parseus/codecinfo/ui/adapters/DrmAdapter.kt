package com.parseus.codecinfo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.databinding.DrmAdapterRowBinding
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.fragments.DetailsFragment
import com.parseus.codecinfo.utils.buildContainerTransform
import com.parseus.codecinfo.utils.getActivity
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.getSecondaryColor
import com.parseus.codecinfo.utils.isInTwoPaneMode

class DrmAdapter(private val drmList: List<DrmSimpleInfo>) : RecyclerView.Adapter<DrmAdapter.DrmInfoViewHolder>() {

    private val sortedList = SortedList(DrmSimpleInfo::class.java, object : SortedListAdapterCallback<DrmSimpleInfo>(this) {
        override fun compare(o1: DrmSimpleInfo, o2: DrmSimpleInfo): Int {
            return o1.drmName.compareTo(o2.drmName)
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

        override fun areContentsTheSame(oldItem: DrmSimpleInfo, newItem: DrmSimpleInfo): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(item1: DrmSimpleInfo, item2: DrmSimpleInfo): Boolean {
            return item1.id == item2.id
        }

    })

    fun add(infoList: List<DrmSimpleInfo>) {
        sortedList.addAll(infoList)
    }

    fun replaceAll(infoList: List<DrmSimpleInfo>) {
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
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrmInfoViewHolder {
        val binding = DrmAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DrmInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrmInfoViewHolder, position: Int) {
        val drmItem = drmList[position]
        holder.bindDrmInfo(drmItem, position)
    }

    override fun getItemCount() = drmList.size

    class DrmInfoViewHolder(binding: DrmAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val layout = binding.simpleDrmRow
        private val drmId = binding.drmId
        private val drmName = binding.drmName
        private val moreInfo = binding.moreInfo

        fun bindDrmInfo(drmSimpleInfo: DrmSimpleInfo, position: Int) {
            drmId.setTextColor(getPrimaryColor(drmId.context))
            drmName.text = drmSimpleInfo.drmName
            drmName.setTextColor(getSecondaryColor(drmName.context))
            if (itemView.context.isInTwoPaneMode()) {
                moreInfo.visibility = View.GONE
            }
            layout.contentDescription = layout.context.getString(R.string.drm_row_content_description,
                    position, drmName)

            layout.transitionName = "$drmName"
            layout.setOnClickListener {
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
                        fragment.arguments = bundleOf(
                            "drmName" to drmSimpleInfo.drmName,
                            "drmUuid" to drmSimpleInfo.drmUuid
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
                            addSharedElement(layout, layout.transitionName!!)
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