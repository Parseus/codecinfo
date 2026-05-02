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

package com.parseus.codecinfo.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.withTranslation

/**
 * Copy of [android.text.style.BulletSpan] from SDK for Android 9.0
 * with removed internal code, additional improvements and converted to Kotlin.
 */
class ImprovedBulletSpan(
    @Px val bulletRadius: Int = STANDARD_BULLET_RADIUS,
    @Px val gapWidth: Int = STANDARD_GAP_WIDTH,
    @ColorInt val color: Int = STANDARD_COLOR,
    val wantColor: Boolean = false
) : LeadingMarginSpan {

    companion object {
        private const val STANDARD_BULLET_RADIUS = 4
        private const val STANDARD_GAP_WIDTH = 10
        private const val STANDARD_COLOR = 0
    }

    private var bulletPath: Path? = null

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
        if (text is Spanned && text.getSpanStart(this) == start) {
            val style = paint.style
            val oldColor = paint.color

            if (wantColor) {
                paint.color = color
            }

            paint.style = Paint.Style.FILL

            val fontMetrics = paint.fontMetrics
            val yPosition = baseline + (fontMetrics.ascent + fontMetrics.descent) / 2f
            val xPosition = (x + dir * bulletRadius).toFloat()

            // Android 10 has improved antialiasing for drawCircle(),
            // so that workaround is not needed on more modern API levels.
            if (Build.VERSION.SDK_INT < 29 && canvas.isHardwareAccelerated) {
                if (bulletPath == null) {
                    bulletPath = Path().apply {
                        addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
                    }
                }

                canvas.withTranslation(xPosition, yPosition) {
                    drawPath(bulletPath!!, paint)
                }
            } else {
                canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)
            }

            if (wantColor) {
                paint.color = oldColor
            }

            paint.style = style
        }
    }

    override fun toString(): String {
        return "BulletSpan{bulletRadius=$bulletRadius, gapWidth=$gapWidth, color=${"%08X".format(color)}}"
    }
}