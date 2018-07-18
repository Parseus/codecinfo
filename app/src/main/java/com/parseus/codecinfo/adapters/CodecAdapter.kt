package com.parseus.codecinfo.adapters

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.fragments.CodecDetailsDialogFragment
import com.parseus.codecinfo.fragments.CodecDetailsFragment
import com.parseus.codecinfo.inflate

class CodecAdapter(private val codecList: List<CodecSimpleInfo>) : RecyclerView.Adapter<CodecAdapter.CodecInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodecInfoViewHolder {
        val inflatedView = parent.inflate(R.layout.codec_adapter_row)

        return CodecInfoViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: CodecInfoViewHolder, position: Int) {
        val codecInfoItem = codecList[position]
        holder.bindCodecInfo(codecInfoItem)
    }

    override fun getItemCount() = codecList.size

    class CodecInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val layout: View = view.findViewById(R.id.simpleCodecRow)
        private val codecId: TextView = view.findViewById(R.id.codec_name)
        private val codecName: TextView = view.findViewById(R.id.codec_full_name)
        private val codecType: TextView = view.findViewById(R.id.codec_type)
        private val moreInfo: TextView = view.findViewById(R.id.more_info)

        fun bindCodecInfo(codecInfo: CodecSimpleInfo) {
            codecId.text = codecInfo.codecId
            codecName.text = codecInfo.codecName
            codecType.text = itemView.resources.getString(
                    if (codecInfo.isEncoder) R.string.encoder else R.string.decoder)

            if (codecInfo.isAudio && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                moreInfo.visibility = View.GONE

                if (itemView.context.resources.getBoolean(R.bool.twoPaneMode)) {
                    layout.setOnClickListener {
                        val detailsFragment = CodecDetailsFragment()
                        val bundle = Bundle().apply {
                            putString("codecName", itemView.resources.getString(R.string.no_info))
                        }
                        detailsFragment.arguments = bundle
                        val activity = (layout.context as FragmentActivity)
                        activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.codecDetailsFragment, detailsFragment).commit()

                    }
                }
            } else {
                if (itemView.context.resources.getBoolean(R.bool.twoPaneMode)) {
                    moreInfo.visibility = View.GONE
                }

                layout.setOnClickListener {
                    val detailsFragment = if (itemView.context.resources.getBoolean(R.bool.twoPaneMode)) {
                        CodecDetailsFragment()
                    } else {
                        CodecDetailsDialogFragment()
                    }
                    val bundle = Bundle().apply {
                        putString("codecId", codecInfo.codecId)
                        putString("codecName", codecInfo.codecName)
                    }
                    detailsFragment.arguments = bundle
                    val activity = (layout.context as FragmentActivity)

                    if (itemView.context.resources.getBoolean(R.bool.twoPaneMode)) {
                        activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.codecDetailsFragment, detailsFragment).commit()
                    } else {
                        val transaction = activity.supportFragmentManager.beginTransaction()
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        transaction.add(android.R.id.content, detailsFragment).addToBackStack(null).commit()
                    }
                }
            }
        }

    }

}