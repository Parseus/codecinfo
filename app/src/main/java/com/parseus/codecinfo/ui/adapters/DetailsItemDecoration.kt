package com.parseus.codecinfo.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.utils.getAttributeColor

@SuppressLint("DiscouragedApi")
class DetailsItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val thickness: Int

    // Since the TV flavor doesn't have MDC-Android,
    // dynamically fall back to system attributes or defaults.
    init {
        val colorOutlineVariantResId = context.resources.getIdentifier("colorOutlineVariant", "attr", context.packageName)
        paint.color = if (colorOutlineVariantResId != 0) {
            context.getAttributeColor(colorOutlineVariantResId)
        } else {
            val typedValue = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.listDivider, typedValue, true)) {
                typedValue.data
            } else {
                0x1FFFFFFF
            }
        }

        val thicknessResId = context.resources.getIdentifier("material_divider_thickness", "dimen", context.packageName)
        thickness = if (thicknessResId != 0) {
            context.resources.getDimensionPixelSize(thicknessResId)
        } else {
            context.resources.displayMetrics.density.toInt()
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter as? DetailsAdapter ?: return
        
        parent.children.forEach { view ->
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION || position >= adapter.itemCount - 1) return@forEach

            val currentItem = adapter.currentList[position]
            val nextItem = adapter.currentList[position + 1]

            val shouldShowDivider = when (currentItem) {
                is DetailItem.Header -> true
                is DetailItem.KnownProblemItem if nextItem is DetailItem.PropertyItem -> true
                else -> false
            }

            if (shouldShowDivider) {
                val top = view.bottom + (view.layoutParams as RecyclerView.LayoutParams).bottomMargin
                val bottom = top + thickness
                c.drawRect(
                    parent.paddingLeft.toFloat(),
                    top.toFloat(),
                    (parent.width - parent.paddingRight).toFloat(),
                    bottom.toFloat(),
                    paint
                )
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter as? DetailsAdapter ?: return

        if (position == RecyclerView.NO_POSITION || position >= adapter.itemCount - 1) {
            outRect.setEmpty()
            return
        }

        val currentItem = adapter.currentList[position]
        val nextItem = adapter.currentList[position + 1]

        val shouldShowDivider = when (currentItem) {
            is DetailItem.Header -> true
            is DetailItem.KnownProblemItem if nextItem is DetailItem.PropertyItem -> true
            else -> false
        }

        if (shouldShowDivider) {
            outRect.set(0, 0, 0, thickness)
        } else {
            outRect.setEmpty()
        }
    }
}
