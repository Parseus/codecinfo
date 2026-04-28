package com.parseus.codecinfo.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.parseus.codecinfo.R

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
            ACTION_OK_ID -> finishGuidedStepSupportFragments()
        }
    }
    companion object {

        private const val ACTION_CHANGELOG_ID = 1L
        private const val ACTION_LICENSES_ID = 2L
        private const val ACTION_OK_ID = 5L
    }

}