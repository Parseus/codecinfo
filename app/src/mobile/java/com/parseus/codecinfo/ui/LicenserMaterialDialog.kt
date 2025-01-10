package com.parseus.codecinfo.ui

import android.util.Base64
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.Licenser
import com.parseus.codecinfo.R
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.updateButtonColors

class LicenserMaterialDialog(val activity: AppCompatActivity) : Licenser() {

    private val alertDialogBuilder = MaterialAlertDialogBuilder(activity)
    private var alertDialog: AlertDialog? = null
    private val webView = HardenedWebView(activity)

    init {
        val container = LinearLayout(activity)
        container.orientation = LinearLayout.VERTICAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, activity.resources.getDimensionPixelSize(
            R.dimen.licenses_dialog_body_margin), 0, 0)
        container.addView(webView, layoutParams)
        alertDialogBuilder.setView(container)
    }

    fun setTitle(@StringRes titleResId: Int): LicenserMaterialDialog {
        alertDialogBuilder.setTitle(titleResId)
        return this
    }

    fun setPositiveButton(@StringRes text: Int): LicenserMaterialDialog {
        alertDialogBuilder.setPositiveButton(activity.resources.getString(text), null)
        return this
    }

    override fun setLibrary(library: Library): LicenserMaterialDialog {
        super.setLibrary(library)
        return this
    }

    override fun setBackgroundColor(backgroundColor: Int): LicenserMaterialDialog {
        super.setBackgroundColor(backgroundColor)
        return this
    }

    fun show() {
        if (webView.url == null) {
            webView.loadData(
                Base64.encodeToString(getDialogHTMLContent(activity).toByteArray(), Base64.NO_PADDING),
                "text/html; charset=UTF-8", "base64")
        }
        if (alertDialog == null) {
            alertDialog = alertDialogBuilder.create()

        }
        alertDialog!!.run {
            show()
            if (isDynamicThemingEnabled(activity) && !isNativeMonetAvailable()) {
                applyMonet()
            }
            updateButtonColors(alertDialogBuilder.context)
        }
    }

}