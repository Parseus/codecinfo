package com.parseus.codecinfo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.databinding.CodecAdapterRowBinding
import com.parseus.codecinfo.fragments.CodecDetailsDialogFragment
import com.parseus.codecinfo.fragments.CodecDetailsFragment
import com.parseus.codecinfo.isInTwoPaneMode

class CodecAdapter(private val codecList: List<CodecSimpleInfo>) : RecyclerView.Adapter<CodecAdapter.CodecInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodecInfoViewHolder {
        val binding = CodecAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CodecInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CodecInfoViewHolder, position: Int) {
        val codecInfoItem = codecList[position]
        holder.bindCodecInfo(codecInfoItem)
    }

    override fun getItemCount() = codecList.size

    class CodecInfoViewHolder(binding: CodecAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val layout = binding.simpleCodecRow
        private val codecId = binding.codecName
        private val codecName = binding.codecFullName
        private val codecType = binding.codecType
        private val moreInfo = binding.moreInfo

        fun bindCodecInfo(codecInfo: CodecSimpleInfo) {
            codecId.text = codecInfo.codecId
            codecName.text = codecInfo.codecName
            codecType.text = itemView.resources.getString(
                    if (codecInfo.isEncoder) R.string.encoder else R.string.decoder)
            if (itemView.context.isInTwoPaneMode()) {
                moreInfo.visibility = View.GONE
            }

            layout.setOnClickListener {
                val activity = (layout.context as FragmentActivity)
                val detailsFragment = if (activity.isInTwoPaneMode()) {
                    CodecDetailsFragment()
                } else {
                    CodecDetailsDialogFragment()
                }
                detailsFragment.arguments = bundleOf(
                        "codecId" to codecInfo.codecId,
                        "codecName" to codecInfo.codecName
                )

                activity.supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    if (activity.isInTwoPaneMode()) {
                        replace(R.id.codecDetailsFragment, detailsFragment)
                    } else {
                        add(android.R.id.content, detailsFragment)
                        addToBackStack(null)
                    }
                }
            }
        }

    }

}