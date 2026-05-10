package com.parseus.codecinfo.ui.utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.MenuItemCompat
import com.google.android.material.tooltip.TooltipDrawable

/**
 * Helper to apply Material Design 3 tooltips to views and menu items.
 * Uses [TooltipDrawable] to ensure M3 styling and bypass native tooltip bugs on API 26.
 */
@SuppressLint("RestrictedApi")
object MaterialTooltipHelper {

    private const val HOVER_DELAY_MS = 500L
    private val handler = Handler(Looper.getMainLooper())
    private var activePopup: PopupWindow? = null
    private var pendingShow: Runnable? = null

    @SuppressLint("RestrictedApi")
    fun applyTooltip(view: View, text: CharSequence) {
        view.setOnHoverListener(null)
        view.setOnLongClickListener(null)

        val tooltipDrawable = TooltipDrawable.createFromAttributes(
            view.context, null, 0, 0
        ).apply {
            this.text = text
        }

        view.setOnHoverListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    scheduleShowTooltip(v, tooltipDrawable)
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    cancelPendingShow()
                    hideTooltip()
                }
            }
            false
        }

        view.setOnLongClickListener { v ->
            showTooltip(v, tooltipDrawable)
            true
        }
    }

    fun applyTooltip(menuItem: MenuItem, text: CharSequence) {
        MenuItemCompat.setTooltipText(menuItem, text)
    }

    private fun scheduleShowTooltip(view: View, tooltip: TooltipDrawable) {
        cancelPendingShow()
        pendingShow = Runnable {
            showTooltip(view, tooltip)
        }.also {
            handler.postDelayed(it, HOVER_DELAY_MS)
        }
    }

    private fun cancelPendingShow() {
        pendingShow?.let { handler.removeCallbacks(it) }
        pendingShow = null
    }

    private fun showTooltip(view: View, tooltip: TooltipDrawable) {
        if (!view.isAttachedToWindow) return
        hideTooltip()

        val context = view.context
        val popupView = View(context).apply {
            background = tooltip
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // TooltipDrawable handles its own intrinsic size based on text
        val width = tooltip.intrinsicWidth
        val height = tooltip.intrinsicHeight

        val popup = PopupWindow(popupView, width, height).apply {
            isClippingEnabled = false
            isOutsideTouchable = true
            elevation = 4f
        }

        val location = IntArray(2)
        view.getLocationInWindow(location)
        val viewCenterX = location[0] + view.width / 2
        val x = viewCenterX - width / 2
        val y = location[1] - height - 8 // 8dp offset

        popup.showAtLocation(view.rootView, Gravity.NO_GRAVITY, x, y)
        activePopup = popup
    }

    private fun hideTooltip() {
        activePopup?.dismiss()
        activePopup = null
    }
}