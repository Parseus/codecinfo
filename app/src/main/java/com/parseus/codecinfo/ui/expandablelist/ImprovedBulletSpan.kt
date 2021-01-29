/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parseus.codecinfo.ui.expandablelist

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import androidx.core.graphics.withSave

/**
 * Copy of [android.text.style.BulletSpan] from SDK for Android 9.0
 * with removed internal code and converted to Kotlin.
 */
class ImprovedBulletSpan(
        val bulletRadius: Int = STANDARD_BULLET_RADIUS,
        val gapWidth: Int = STANDARD_GAP_WIDTH,
        val color: Int = STANDARD_COLOR
) : LeadingMarginSpan {

    companion object {
        // Bullet is slightly bigger to avoid aliasing artifacts on mdpi devices.
        private const val STANDARD_BULLET_RADIUS = 4
        private const val STANDARD_GAP_WIDTH = 2
        private const val STANDARD_COLOR = 0
    }

    private lateinit var bulletPath: Path

    override fun getLeadingMargin(first: Boolean): Int {
        return 2 * bulletRadius + gapWidth
    }

    override fun drawLeadingMargin(
            canvas: Canvas, paint: Paint, x: Int, dir: Int,
            top: Int, baseline: Int, bottom: Int,
            text: CharSequence, start: Int, end: Int,
            first: Boolean,
            layout: Layout?
    ) {
        if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            paint.style = Paint.Style.FILL

            val yPosition = if (layout != null) {
                val line = layout.getLineForOffset(start)
                layout.getLineBaseline(line).toFloat() - bulletRadius * 2f
            } else {
                (top + bottom) / 2f
            }

            val xPosition = (x + dir * bulletRadius).toFloat()

            if (canvas.isHardwareAccelerated) {
                if (!::bulletPath.isInitialized) {
                    bulletPath = Path()
                    bulletPath.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
                }

                canvas.withSave {
                    translate(xPosition, yPosition)
                    drawPath(bulletPath, paint)
                }
            } else {
                canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)
            }

            paint.style = style
        }
    }
}