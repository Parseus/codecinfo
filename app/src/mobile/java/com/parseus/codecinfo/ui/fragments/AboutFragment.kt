package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.AboutAppFragmentBinding
import com.parseus.codecinfo.utils.*

class AboutFragment : Fragment() {

    private var _binding: AboutAppFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AboutAppFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            showChangelog.setOnClickListener { showChangelog() }
            try {
                appVersion.text = getString(R.string.app_version,
                    requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName)
            } catch (e: Exception) {
                appVersion.isVisible = false
            }
            showLicenses.setOnClickListener { showLicensesDialog(requireActivity() as AppCompatActivity) }
            goToGithub.setOnClickListener { goToAppsGitHubPage() }

            if (SHOW_RATE_APP) {
                rateApp.setOnClickListener { activity?.let { launchStoreIntent(it) } }
            } else {
                rateApp.isVisible = false
            }
            reportBugs.setOnClickListener { goToIssuePage() }
            sendFeedback.setOnClickListener { sendFeedbackEmail() }

            supportHeaderText.setTextColor(getSecondaryColor(requireContext()))
        }
    }

    private fun showChangelog() {
        context?.let {
            val dialogBuilder = MaterialAlertDialogBuilder(it)
                .setTitle(R.string.about_changelog)
                .setView(R.layout.about_app_changelog)
            val dialog = dialogBuilder.updateBackgroundColor(dialogBuilder.context)
                .setPositiveButton(android.R.string.ok, null).create()
            dialog.show()
            dialog.applyMonet()
            dialog.updateButtonColors(dialogBuilder.context)
        }
    }

    private fun goToAppsGitHubPage() {
        if (isAdded) {
            val issuePageIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_PAGE))
            issuePageIntent.addFlags(externalAppIntentFlags)
            startActivity(issuePageIntent)
        }
    }

    private fun goToIssuePage() {
        if (isAdded) {
            val issuePageIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ISSUE_PAGE))
            issuePageIntent.addFlags(externalAppIntentFlags)
            startActivity(issuePageIntent)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val GITHUB_PAGE = "https://github.com/Parseus/codecinfo"
        private const val ISSUE_PAGE = "https://github.com/Parseus/codecinfo/issues"
    }

}