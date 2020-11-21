package com.parseus.codecinfo.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.parseus.codecinfo.fragments.CodecFragment

class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return CodecFragment().apply {
            val bundle = Bundle()
            bundle.putBoolean("isAudio", position == 0)
            arguments = bundle
        }
    }

    override fun getItemCount() = 2

}