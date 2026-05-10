package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.WearFragmentAboutBinding
import com.parseus.codecinfo.ui.adapters.MoreAction
import com.parseus.codecinfo.ui.adapters.WearMoreAdapter
import com.parseus.codecinfo.utils.applyRotaryInput
import com.parseus.codecinfo.utils.externalAppIntentFlags

class WearAboutFragment : Fragment() {

    private var _binding: WearFragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearFragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                parentFragmentManager.popBackStack()
            }
        })

        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        binding.versionInfo.text = getString(R.string.app_version, packageInfo.versionName)

        val actions = listOf(
            MoreAction(ID_CHANGELOG, R.string.about_changelog, R.drawable.ic_changelog),
            MoreAction(ID_LICENSES, R.string.about_licenses, R.drawable.ic_licenses),
            MoreAction(ID_BUG_REPORT, R.string.about_report_bugs, R.drawable.ic_report_bugs),
            MoreAction(ID_GITHUB, R.string.source_code_link, R.drawable.ic_github)
        )

        val adapter = WearMoreAdapter { action ->
            when (action.id) {
                ID_CHANGELOG -> parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, WearChangelogFragment())
                    .addToBackStack(null).commit()
                ID_LICENSES -> parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, WearLicensesFragment())
                    .addToBackStack(null).commit()
                ID_BUG_REPORT -> openUrl(ISSUE_PAGE)
                ID_GITHUB -> openUrl(GITHUB_PAGE)
            }
        }

        binding.wearableRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(requireContext())
            this.adapter = adapter
            applyRotaryInput()
            requestFocus()
        }
        adapter.submitList(actions)
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(externalAppIntentFlags)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.no_apps_for_action, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.wearableRecyclerView.requestFocus()
    }

    companion object {
        private const val GITHUB_PAGE = "https://github.com/Parseus/codecinfo"
        private const val ISSUE_PAGE = "https://github.com/Parseus/codecinfo/issues"
        private const val ID_CHANGELOG = 1
        private const val ID_LICENSES = 2
        private const val ID_BUG_REPORT = 3
        private const val ID_GITHUB = 4
    }
}