package com.parseus.codecinfo.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.parseus.codecinfo.InfoType
import com.parseus.codecinfo.fragments.ItemFragment

class PagerAdapter(fragmentManager: FragmentManager,
                   lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return ItemFragment().apply {
            val bundle = Bundle()
            bundle.putInt("infoType", position)
            arguments = bundle
        }
    }

    override fun getItemCount() = InfoType.INFO_TYPE_COUNT

}