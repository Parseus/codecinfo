package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.databinding.WearFragmentMoreBinding
import com.parseus.codecinfo.ui.WearSearchActivity
import com.parseus.codecinfo.ui.adapters.MoreAction
import com.parseus.codecinfo.ui.adapters.WearMoreAdapter
import com.parseus.codecinfo.ui.settings.SettingsContract
import com.parseus.codecinfo.utils.applyRotaryInput
import com.parseus.codecinfo.viewmodels.ItemsViewModel

class WearMoreFragment : Fragment() {

    private var _binding: WearFragmentMoreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ItemsViewModel by activityViewModels()

    private val settingsContract = registerForActivityResult(SettingsContract()) { result ->
        if (result.shouldReloadLists() || result.saveDetailsToLogcatChanged) {
            viewModel.refreshData(requireContext())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearFragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                parentFragmentManager.popBackStack()
            }
        })

        val actions = mutableListOf<MoreAction>().apply {
            add(MoreAction(ID_SEARCH, R.string.action_search, R.drawable.ic_search))
            add(MoreAction(ID_SHARE, R.string.action_share, R.drawable.ic_share)) // Add this
            if (DEVICE_PROBLEMS_DB.any { it.isAffected(requireContext(), null) }) {
                add(MoreAction(ID_WARNINGS, R.string.action_warning, R.drawable.ic_warning))
            }
            add(MoreAction(ID_SETTINGS, R.string.action_settings, R.drawable.ic_settings))
            add(MoreAction(ID_ABOUT, R.string.about_app, R.drawable.ic_info))
        }

        val adapter = WearMoreAdapter { action ->
            when (action.id) {
                ID_SEARCH -> startActivity(Intent(requireContext(), WearSearchActivity::class.java))
                ID_SHARE -> parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, WearShareFragment())
                    .addToBackStack(null).commit()
                ID_WARNINGS -> parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, WearDeviceIssuesFragment())
                    .addToBackStack(null).commit()
                ID_SETTINGS -> settingsContract.launch(null)
                ID_ABOUT -> parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, WearAboutFragment())
                    .addToBackStack(null).commit()
            }
        }

        binding.wearableRecyclerView.apply {
            setHasFixedSize(true)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(requireContext())
            this.adapter = adapter
            applyRotaryInput()
            requestFocus()
        }
        adapter.submitList(actions)
    }

    override fun onResume() {
        super.onResume()
        binding.wearableRecyclerView.requestFocus()
    }

    override fun onDestroyView() {
        binding.wearableRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ID_SEARCH = 1
        private const val ID_SHARE = 2
        private const val ID_WARNINGS = 3
        private const val ID_SETTINGS = 4
        private const val ID_ABOUT = 5
    }
}