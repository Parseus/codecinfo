package com.parseus.codecinfo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.buildContainerTransform
import com.parseus.codecinfo.databinding.DrmAdapterRowBinding
import com.parseus.codecinfo.drm.DrmSimpleInfo
import com.parseus.codecinfo.fragments.DetailsFragment
import com.parseus.codecinfo.isInTwoPaneMode

class DrmAdapter(private val drmList: List<DrmSimpleInfo>) : RecyclerView.Adapter<DrmAdapter.DrmInfoViewHolder>() {

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
        private val drmName = binding.drmName
        private val moreInfo = binding.moreInfo

        fun bindDrmInfo(drmSimpleInfo: DrmSimpleInfo, position: Int) {
            drmName.text = drmSimpleInfo.drmName
            if (itemView.context.isInTwoPaneMode()) {
                moreInfo.visibility = View.GONE
            }
            layout.contentDescription = layout.context.getString(R.string.drm_row_content_description,
                    position, drmName)

            ViewCompat.setTransitionName(layout, "$drmName")
            layout.setOnClickListener {
                val activity = (itemView.context as AppCompatActivity)

                // Do not create the same fragment again.
                activity.supportFragmentManager
                        .findFragmentByTag(activity.getString(R.string.details_fragment_tag))?.let {
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

                    if (!activity.isInTwoPaneMode()) {
                        fragment.sharedElementEnterTransition = buildContainerTransform(layout, true)
                        fragment.sharedElementReturnTransition = buildContainerTransform(layout, false)
                    }
                }

                activity.supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    if (activity.isInTwoPaneMode()) {
                        replace(R.id.itemDetailsFragment, detailsFragment,
                                activity.getString(R.string.details_fragment_tag))
                    } else {
                        addSharedElement(layout, ViewCompat.getTransitionName(layout)!!)
                        replace(R.id.content_fragment, detailsFragment,
                                activity.getString(R.string.details_fragment_tag))
                        addToBackStack(null)

                        activity.supportActionBar!!.apply {
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