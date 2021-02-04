package com.parseus.codecinfo.ui.adapters

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.ui.fragments.ItemFragment

class PagerAdapter(fragmentManager: FragmentManager,
                   lifecycle: Lifecycle,
                   private val searchListenerList: MutableList<SearchView.OnQueryTextListener>)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return ItemFragment().apply {
            val bundle = Bundle()
            bundle.putInt("infoType", position)
            arguments = bundle
            val existingFragment = searchListenerList.find {
                it is ItemFragment && it.requireArguments().getInt("infoType")  == position
            }
            if (existingFragment != null) {
                searchListenerList.remove(existingFragment)
            }
            searchListenerList.add(this)
        }
    }

    override fun getItemCount() = InfoType.INFO_TYPE_COUNT

}