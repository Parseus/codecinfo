package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.data.WEAR_LIBRARIES
import com.parseus.codecinfo.databinding.WearFragmentLicensesBinding
import com.parseus.codecinfo.ui.adapters.WearLicensesAdapter
import com.parseus.codecinfo.utils.applyRotaryInput

class WearLicensesFragment : Fragment() {

    private var _binding: WearFragmentLicensesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearFragmentLicensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                parentFragmentManager.popBackStack()
            }
        })

        val adapter = WearLicensesAdapter()
        binding.wearableRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(requireContext())
            this.adapter = adapter
            applyRotaryInput()
            requestFocus()
        }
        adapter.submitList(WEAR_LIBRARIES)
    }

    override fun onResume() {
        super.onResume()
        binding.wearableRecyclerView.requestFocus()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}