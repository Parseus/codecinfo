package com.parseus.codecinfo.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.fragment.app.commit
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.parseus.codecinfo.R
import com.parseus.codecinfo.utils.ToastCompat
import com.parseus.codecinfo.utils.copyToClipboard
import com.parseus.codecinfo.utils.externalAppIntentFlags

class TvAboutFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val title = getString(R.string.about_app)
        val packageInfo = if (Build.VERSION.SDK_INT >= 33) {
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        }

        val sb = StringBuilder()
        sb.append(getString(R.string.app_version, packageInfo.versionName)).append('\n')
        sb.append(getString(R.string.copyright)).append('\n')
        sb.append(getString(R.string.source_code_link))
        val description = sb.toString()

        val icon = AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_launcher)
        return GuidanceStylist.Guidance(title, description, null, icon)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        actions.add(GuidedAction.Builder(requireContext())
            .id(ACTION_CHANGELOG_ID)
            .title(R.string.about_changelog)
            .icon(R.drawable.ic_changelog)
            .build())
        actions.add(GuidedAction.Builder(requireContext())
            .id(ACTION_LICENSES_ID)
            .title(R.string.about_licenses)
            .icon(R.drawable.ic_licenses)
            .build())
        actions.add(GuidedAction.Builder(requireContext())
            .id(ACTION_FEEDBACK_ID)
            .title(R.string.about_send_feedback)
            .icon(R.drawable.ic_email)
            .build())
        actions.add(GuidedAction.Builder(requireContext())
            .id(ACTION_BUG_REPORT_ID)
            .title(R.string.about_report_bugs)
            .icon(R.drawable.ic_report_bugs)
            .build())
        actions.add(GuidedAction.Builder(requireContext())
            .id(ACTION_OK_ID)
            .title(android.R.string.ok)
            .build())
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_CHANGELOG_ID -> {
                parentFragmentManager.commit {
                    replace(android.R.id.content, TvChangelogFragment())
                    addToBackStack(null)
                }
            }
            ACTION_LICENSES_ID -> {
                parentFragmentManager.commit {
                    replace(android.R.id.content, TvLicensesFragment())
                    addToBackStack(null)
                }
            }
            ACTION_FEEDBACK_ID -> sendFeedbackEmail()
            ACTION_BUG_REPORT_ID -> reportBug()
            ACTION_OK_ID -> finishGuidedStepSupportFragments()
        }
    }

    private fun sendFeedbackEmail() {
        val feedbackEmail = getString(R.string.feedback_email)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
        } else if (isAdded) {
            try {
                requireContext().copyToClipboard("email", feedbackEmail)

                ToastCompat.makeText(
                    requireContext(),
                    R.string.no_email_apps_clipboard,
                    Toast.LENGTH_LONG
                ).show()
            } catch (_: Exception) {
                ToastCompat.makeText(
                    requireContext(),
                    R.string.no_email_apps,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun reportBug() {
        val intent = Intent(Intent.ACTION_VIEW, ISSUE_PAGE.toUri())
        intent.addFlags(externalAppIntentFlags)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            ToastCompat.makeText(requireContext(), R.string.no_apps_for_action, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val ISSUE_PAGE = "https://github.com/Parseus/codecinfo/issues"

        private const val ACTION_CHANGELOG_ID = 1L
        private const val ACTION_LICENSES_ID = 2L
        private const val ACTION_FEEDBACK_ID = 3L
        private const val ACTION_BUG_REPORT_ID = 4L
        private const val ACTION_OK_ID = 5L
    }

}