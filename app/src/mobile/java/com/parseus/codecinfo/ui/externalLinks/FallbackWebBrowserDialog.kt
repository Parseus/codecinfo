package com.parseus.codecinfo.ui.externalLinks

import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.google.android.material.shape.MaterialShapeDrawable
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.FallbackWebBrowserDialogBinding
import com.parseus.codecinfo.utils.getColorOnSurface
import com.parseus.codecinfo.utils.getOnPrimaryColor
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.isNightMode
import com.parseus.codecinfo.utils.updateToolBarColor

class FallbackWebBrowserDialog : DialogFragment() {

    private var url: Uri? = null

    private val viewModel: ExternalLinksViewModel by activityViewModels()

    private var _binding: FallbackWebBrowserDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FallbackWebBrowserDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            url = BundleCompat.getParcelable(savedInstanceState, EXTRA_URL, Uri::class.java)
        } else if (arguments != null) {
            url = BundleCompat.getParcelable(requireArguments(), EXTRA_URL, Uri::class.java)
        }
        if (url == null) {
            dismiss()
            return
        } else {
            viewModel.urlOpened = url
        }

        super.onViewCreated(view, savedInstanceState)

        if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
            view.applyMonetRecursively()
        }

        val urlString = url.toString()
        with (binding.toolbar) {
            title = urlString
            updateToolBarColor(requireContext())
            val closeIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_close)!!.also {
                it.setTint(if (requireContext().isNightMode()) {
                    getColorOnSurface(requireContext())
                } else {
                    getOnPrimaryColor(requireContext())
                })
            }
            navigationIcon = closeIcon
            navigationContentDescription = getString(R.string.action_close)
            setNavigationOnClickListener {
                viewModel.launchExternalLink.value = null
                viewModel.urlOpened = null
                dismiss()
            }
        }
        binding.webView.allowedHosts = arrayListOf(ANDROID_DOCS_HOST, GITHUB_HOST)
        binding.webView.loadUrl(urlString)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.window?.run {
                requestFeature(Window.FEATURE_NO_TITLE)
                setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                setWindowBackground(this)
            }
        }
    }

    private fun setWindowBackground(window: Window) {
        val materialShapeDrawable = MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        // dialogCornerRadius first appeared in Android Pie
        if (Build.VERSION.SDK_INT >= 28) {
            val dialogCornerRadiusValue = TypedValue()
            ContextThemeWrapper(requireContext(), theme).theme.resolveAttribute(
                androidx.appcompat.R.attr.dialogCornerRadius, dialogCornerRadiusValue, true)
            val dialogCornerRadius =
                dialogCornerRadiusValue.getDimension(requireContext().resources.displayMetrics)
            if (dialogCornerRadiusValue.type == TypedValue.TYPE_DIMENSION && dialogCornerRadius >= 0) {
                materialShapeDrawable.setCornerSize(dialogCornerRadius)
            }
        }

        window.setBackgroundDrawable(materialShapeDrawable)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ANDROID_DOCS_HOST = "developer.android.com"
        private const val GITHUB_HOST = "github.com"

        private const val EXTRA_URL = "EXTRA_URL"

        @JvmStatic fun showDialog(url: Uri, fragmentManager: FragmentManager) {
            FallbackWebBrowserDialog().run {
                setStyle(STYLE_NORMAL, R.style.Theme_CodecInfo_Dialog_MinWidth)
                arguments = Bundle().apply { putParcelable(EXTRA_URL, url) }
                show(fragmentManager, null)
            }
        }
    }

}