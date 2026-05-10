package com.parseus.codecinfo.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.ui.fragments.WearItemFragment
import com.parseus.codecinfo.ui.fragments.WearMoreFragment

class WearPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    // 3 categories (Audio, Video, DRM) + 1 "More" hub
    override fun getItemCount(): Int = InfoType.INFO_TYPE_COUNT + 1

    override fun createFragment(position: Int): Fragment {
        return if (position < InfoType.INFO_TYPE_COUNT) {
            WearItemFragment.newInstance(InfoType.fromInt(position))
        } else {
            WearMoreFragment()
        }
    }
}