package com.parseus.codecinfo.fragments

import android.annotation.TargetApi
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.adapters.DetailsAdapter
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.databinding.ItemDetailsFragmentLayoutBinding
import com.parseus.codecinfo.drm.DrmVendor
import com.parseus.codecinfo.drm.getDetailedDrmInfo
import java.util.*

@TargetApi(18)
class DetailsFragment : Fragment() {

    private var _binding: ItemDetailsFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    var codecId: String? = null
    var codecName: String? = null

    var drmName: String? = null
    var drmUuid: UUID? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ItemDetailsFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = savedInstanceState ?: arguments
        bundle?.let {
            codecId = it.getString("codecId")
            codecName = it.getString("codecName")
            drmName = it.getString("drmName")
            drmUuid = it.getSerializable("drmUuid") as UUID?

            val infoMap = when {
                codecId != null && codecName != null ->
                    getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
                drmName != null && drmUuid != null ->
                    getDetailedDrmInfo(requireContext(), DrmVendor.getFromUuid(drmUuid!!))
                else -> null
            }
            infoMap?.let { map -> getFullDetails(map) }
        }
    }

    private fun getFullDetails(infoMap: Map<String, String>) {
        binding.fullCodecInfoName.text = codecName ?: drmName
        val detailsAdapter = DetailsAdapter(infoMap)
        binding.fullCodecInfoContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detailsAdapter
            ViewCompat.setNestedScrollingEnabled(this, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("codecId", codecId)
        outState.putString("codecName", codecName)
        outState.putString("drmName", drmName)
        outState.putSerializable("drmUuid", drmUuid)
    }

}