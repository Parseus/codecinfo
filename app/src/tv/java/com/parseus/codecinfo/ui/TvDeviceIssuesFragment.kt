package com.parseus.codecinfo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.databinding.TvDeviceIssuesLayoutBinding
import com.parseus.codecinfo.ui.adapters.DeviceIssuesAdapter

class TvDeviceIssuesFragment : Fragment() {

    private var _binding: TvDeviceIssuesLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TvDeviceIssuesLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deviceIssuesList.root.adapter = DeviceIssuesAdapter(DEVICE_PROBLEMS_DB)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}