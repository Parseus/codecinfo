package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.databinding.FragmentMainBinding
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.adapters.PagerAdapter
import com.parseus.codecinfo.utils.initializeSamsungGesture
import com.parseus.codecinfo.utils.updateColors

@Suppress("unused")
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabLayout?.let { tabs ->
            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    InfoType.currentInfoType = InfoType.fromInt(tab.position)
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            val viewPager = binding.pager!!
            val pagerAdapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle,
                (activity as? MainActivity)?.searchListeners ?: mutableListOf())
            viewPager.adapter = pagerAdapter

            TabLayoutMediator(tabs, viewPager) { tab, position ->
                val infoType = InfoType.fromInt(position)
                tab.contentDescription = getString(infoType.tabTextResId)
                tab.icon = AppCompatResources.getDrawable(requireContext(), infoType.tabIconResId)
                tab.text = getString(infoType.tabTextResId)
            }.attach()

            initializeSamsungGesture(requireContext(), viewPager, tabs)
        }

        binding.navigationRail?.let { navigationRail ->
            navigationRail.updateColors(requireContext())

            addFragmentToViewHierarchy()

            navigationRail.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.audio -> InfoType.currentInfoType = InfoType.Audio
                    R.id.video -> InfoType.currentInfoType = InfoType.Video
                    R.id.drm -> InfoType.currentInfoType = InfoType.DRM
                }

                addFragmentToViewHierarchy()

                true
            }
        }
    }

    private fun addFragmentToViewHierarchy() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.itemFragment, createInfoFragment())
            .commit()
    }

    private fun createInfoFragment(): Fragment {
        return ItemFragment().apply {
            val bundle = Bundle()
            bundle.putInt("infoType", InfoType.currentInfoType.tabPosition)
            arguments = bundle
            if (this@MainFragment.activity as? MainActivity != null) {
                val searchListenerList = (this@MainFragment.activity as? MainActivity)!!.searchListeners
                val existingFragment = searchListenerList.find {
                    it is ItemFragment && (it.requireArguments().getInt("infoType")
                        == InfoType.currentInfoType.tabPosition)
                }
                if (existingFragment != null) {
                    searchListenerList.remove(existingFragment)
                }
                searchListenerList.add(this)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}