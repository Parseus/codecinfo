package com.parseus.codecinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.parseus.codecinfo.R
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
        val viewPager = binding.pager
        val pagerAdapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            val isAudio = position == 0
            if (isAudio) {
                tab.contentDescription = getString(R.string.category_audio)
                tab.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_audio)
                tab.text = getString(R.string.category_audio)
            } else {
                tab.contentDescription = getString(R.string.category_video)
                tab.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_video)
                tab.text = getString(R.string.category_video)
            }
        }.attach()

        initializeSamsungGesture(requireContext(), viewPager, tabs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}