package com.parseus.codecinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.parseus.codecinfo.InfoType
import com.parseus.codecinfo.adapters.PagerAdapter
import com.parseus.codecinfo.databinding.FragmentMainBinding
import com.parseus.codecinfo.initializeSamsungGesture

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

        val tabs = binding.tabLayout
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                InfoType.currentInfoType = InfoType.fromInt(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        val viewPager = binding.pager
        val pagerAdapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            val infoType = InfoType.fromInt(position)
            tab.contentDescription = getString(infoType.tabTextResId)
            tab.icon = AppCompatResources.getDrawable(requireContext(), infoType.tabIconResId)
            tab.text = getString(infoType.tabTextResId)
        }.attach()

        initializeSamsungGesture(requireContext(), viewPager, tabs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}