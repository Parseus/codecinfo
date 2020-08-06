package com.parseus.codecinfo.adapters

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.parseus.codecinfo.fragments.CodecFragment
import com.parseus.codecinfo.R

class PagerAdapter(private val context: Context, fragmentManager: FragmentManager)
    : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return CodecFragment().apply {
            val bundle = Bundle()
            bundle.putBoolean("isAudio", position == 0)
            arguments = bundle
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.category_audio)
            1 -> context.getString(R.string.category_video)
            else -> null
        }
    }

}