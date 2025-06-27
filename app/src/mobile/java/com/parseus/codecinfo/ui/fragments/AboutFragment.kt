package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.AboutAppFragmentBinding
import com.parseus.codecinfo.ui.ImprovedBulletSpan
import com.parseus.codecinfo.utils.SHOW_RATE_APP
import com.parseus.codecinfo.utils.externalAppIntentFlags
import com.parseus.codecinfo.utils.getOnPrimaryColor
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.getSecondaryColor
import com.parseus.codecinfo.utils.getSurfaceColor
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.isNightMode
import com.parseus.codecinfo.utils.launchStoreIntent
import com.parseus.codecinfo.utils.sendFeedbackEmail
import com.parseus.codecinfo.utils.showLicensesDialog
import com.parseus.codecinfo.utils.updateBackgroundColor
import com.parseus.codecinfo.utils.updateButtonColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source

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
                    requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
                }
                appVersion.text = getString(R.string.app_version, packageInfo.versionName)
            } catch (_: Exception) {
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
                if (requireContext().isNightMode()) {
                    icon.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getSurfaceColor(requireContext()), BlendModeCompat.SRC_ATOP)
                    icon.foreground.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getPrimaryColor(requireContext()), BlendModeCompat.SRC_ATOP)
                } else {
                    icon.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getPrimaryColor(requireContext()), BlendModeCompat.SRC_ATOP)
                    icon.foreground.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getOnPrimaryColor(requireContext()), BlendModeCompat.SRC_ATOP)
                }
            } else if (icon is BitmapDrawable) {
                try {
                    val foregroundIcon = getAppVectorForegroundIcon().toDrawable(resources)
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
            viewLifecycleOwner.lifecycleScope.launch {
                val spannableBuilder = withContext(Dispatchers.Default) {
                    val htmlText: String
                    it.assets.open("changelog.html").source().buffer().use { buffer ->
                        htmlText = buffer.readUtf8()
                    }
                    SpannableStringBuilder(htmlText.parseAsHtml()).apply {
                        val bulletSpans = getSpans(0, length, BulletSpan::class.java)
                        bulletSpans.forEach { span ->
                            val start = getSpanStart(span)
                            val end = getSpanEnd(span)
                            removeSpan(span)
                            setSpan(ImprovedBulletSpan(), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
                val dialogBuilder = MaterialAlertDialogBuilder(it)
                    .setTitle(R.string.about_changelog)
                    .setMessage(spannableBuilder)
                val dialog = dialogBuilder.updateBackgroundColor(dialogBuilder.context)
                    .setPositiveButton(android.R.string.ok, null).show()
                if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
                    dialog.applyMonet()
                }
                dialog.updateButtonColors(dialogBuilder.context)
            }
        }
    }

    private fun goToAppsGitHubPage() {
        if (isAdded) {
            val issuePageIntent = Intent(Intent.ACTION_VIEW, GITHUB_PAGE.toUri())
            issuePageIntent.addFlags(externalAppIntentFlags)
            startActivity(issuePageIntent)
        }
    }

    private fun goToIssuePage() {
        if (isAdded) {
            val issuePageIntent = Intent(Intent.ACTION_VIEW, ISSUE_PAGE.toUri())
            issuePageIntent.addFlags(externalAppIntentFlags)
            startActivity(issuePageIntent)
        }
    }

    private fun getAppVectorForegroundIcon(): Bitmap {
        val foregroundIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_launcher_foreground)!!
        val iconSize = resources.getDimensionPixelSize(R.dimen.about_dialog_app_icon_size)
        val bitmap = createBitmap(iconSize, iconSize)
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