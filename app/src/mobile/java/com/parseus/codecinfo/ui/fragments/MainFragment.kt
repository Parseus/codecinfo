package com.parseus.codecinfo.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import com.google.android.material.tabs.TabLayoutMediator
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.databinding.FragmentMainBinding
import com.parseus.codecinfo.ui.adapters.PagerAdapter
import com.parseus.codecinfo.utils.updateColors
import com.parseus.codecinfo.viewmodels.ItemsViewModel

@Suppress("unused")
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ItemsViewModel by activityViewModels()

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
            val pagerAdapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            viewPager.adapter = pagerAdapter

            TabLayoutMediator(tabs, viewPager) { tab, position ->
                val infoType = InfoType.fromInt(position)
                tab.contentDescription = getString(infoType.tabTextResId)
                tab.icon = AppCompatResources.getDrawable(requireContext(), infoType.tabIconResId)
                tab.text = getString(infoType.tabTextResId)
            }.attach()

            tabs.updateColors(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshDetailsTrigger.collect {
                    binding.pager?.let { pager ->
                        for (i in 0 until (pager.adapter?.itemCount ?: 0)) {
                            val fragment = childFragmentManager.findFragmentByTag("f$i")
                            if (fragment is ItemFragment) {
                                fragment.updateView()
                            }
                        }
                    }
                }
            }
        }

        binding.navigationRail?.let { navigationRail ->
            updateNavigationRail(resources.configuration)

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateNavigationRail(newConfig)
    }

    private fun updateNavigationRail(configuration: Configuration) {
        binding.navigationRail?.let {
            if (configuration.screenWidthDp >= 1200) {
                it.expand()
            } else {
                it.collapse()
            }
            it.updateColors(requireContext())
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}