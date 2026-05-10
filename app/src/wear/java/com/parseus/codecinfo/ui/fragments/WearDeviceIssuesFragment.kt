package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.databinding.WearFragmentDeviceIssuesBinding
import com.parseus.codecinfo.ui.adapters.DeviceIssuesAdapter

class WearDeviceIssuesFragment : Fragment() {

    private var _binding: WearFragmentDeviceIssuesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearFragmentDeviceIssuesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                parentFragmentManager.popBackStack()
            }
        })

        binding.wearableRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(requireContext())
            adapter = DeviceIssuesAdapter(DEVICE_PROBLEMS_DB.filter { it.isAffected(requireContext(), null) })
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}