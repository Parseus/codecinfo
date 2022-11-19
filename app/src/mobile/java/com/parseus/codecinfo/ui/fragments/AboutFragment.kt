package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
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
                val packageInfo = if (Build.VERSION.SDK_INT >= 33) {
                    requireActivity().packageManager.getPackageInfo(requireActivity().packageName, PackageManager.PackageInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
                }
                appVersion.text = getString(R.string.app_version, packageInfo.versionName)
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

            var icon = AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_launcher)
            if (Build.VERSION.SDK_INT >= 26 && icon is AdaptiveIconDrawable && isDynamicThemingEnabled(requireContext())) {
                icon.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getPrimaryColor(requireContext()), BlendModeCompat.SRC_ATOP)
            } else if (icon is BitmapDrawable) {
                try {
                    val foregroundIcon = BitmapDrawable(resources, getAppVectorForegroundIcon())
                    val backgroundIcon = AppCompatResources.getDrawable(requireContext(),
                        R.drawable.legacy_about_app_icon_background)!!
                    backgroundIcon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getPrimaryColor(requireContext()), BlendModeCompat.SRC_ATOP)
                    icon = LayerDrawable((arrayOf(backgroundIcon, foregroundIcon)))
                } catch (_: Throwable) {}
            }
            appIcon.setImageDrawable(icon)
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
            if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
                dialog.applyMonet()
            }
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

    private fun getAppVectorForegroundIcon(): Bitmap {
        val foregroundIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_launcher_foreground)!!
        val iconSize = resources.getDimensionPixelSize(R.dimen.about_dialog_app_icon_size)
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        foregroundIcon.setBounds(0, 0, iconSize, iconSize)
        foregroundIcon.draw(canvas)
        return bitmap
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