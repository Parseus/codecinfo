package com.parseus.codecinfo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.CompoundButton
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.forEach
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayout
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.toArgb
import com.parseus.codecinfo.data.settingsRepository
import kotlin.math.roundToInt

private const val TAG = "ThemeUtils"

fun isNativeMonetAvailable(): Boolean = DynamicColors.isDynamicColorAvailable()

fun isDynamicThemingEnabled(context: Context): Boolean {
    return context.settingsRepository.getSettingsSync().dynamicTheme
}

fun getPrimaryColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_accent1_200)
            } else {
                context.getColor(android.R.color.system_accent1_600)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (context.isNightMode()) {
                monet.getMonetColors().accent1[200]!!.toArgb()
            } else {
                monet.getMonetColors().accent1[600]!!.toArgb()
            }
        }
    } else {
        MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, TAG)
    }
}

fun getOnPrimaryColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_accent1_800)
            } else {
                context.getColor(android.R.color.system_accent1_0)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (context.isNightMode()) {
                monet.getMonetColors().accent1[800]!!.toArgb()
            } else {
                monet.getMonetColors().accent1[0]!!.toArgb()
            }
        }
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimary, TAG)
    }
}

fun getSecondaryColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_accent2_200)
            } else {
                context.getColor(android.R.color.system_accent2_600)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (context.isNightMode()) {
                monet.getMonetColors().accent2[200]!!.toArgb()
            } else {
                monet.getMonetColors().accent2[600]!!.toArgb()
            }
        }
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary, TAG)
    }
}

fun getSecondaryContainerColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_accent2_700)
            } else {
                context.getColor(android.R.color.system_accent2_100)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (context.isNightMode()) {
                monet.getMonetColors().accent2[700]!!.toArgb()
            } else {
                monet.getMonetColors().accent2[100]!!.toArgb()
            }
        }
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondaryContainer, TAG)
    }
}

fun getOnSecondaryContainerColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_accent2_100)
            } else {
                context.getColor(android.R.color.system_accent2_900)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (context.isNightMode()) {
                monet.getMonetColors().accent2[100]!!.toArgb()
            } else {
                monet.getMonetColors().accent2[900]!!.toArgb()
            }
        }
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer, TAG)
    }
}

fun getSurfaceColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_neutral1_900)
            } else {
                context.getColor(android.R.color.system_neutral1_10)
            }
        } else {
            MonetCompat.getInstance().getBackgroundColor(context)
        }
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, TAG)
    }
}

fun getSurfaceContainerHighColor(context: Context): Int {
    return MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainerHigh, TAG)
}

fun getColorOnSurface(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_neutral1_100)
            } else {
                context.getColor(android.R.color.system_neutral1_900)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (context.isNightMode()) {
                monet.getMonetColors().neutral1[100]!!.toArgb()
            } else {
                monet.getMonetColors().neutral1[900]!!.toArgb()
            }
        }
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, TAG)
    }
}

fun getColorOnSurfaceVariant(context: Context): Int {
    return if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (context.isNightMode()) {
                context.getColor(android.R.color.system_neutral2_200)
            } else {
                context.getColor(android.R.color.system_neutral2_700)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (context.isNightMode()) {
                monet.getMonetColors().neutral2[200]!!.toArgb()
            } else {
                monet.getMonetColors().neutral2[700]!!.toArgb()
            }
        }
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, TAG)
    }
}

fun AlertDialog.updateButtonColors(context: Context) {
    (getButton(DialogInterface.BUTTON_POSITIVE) as? MaterialButton)?.updateColors(context)
    (getButton(DialogInterface.BUTTON_NEUTRAL) as? MaterialButton)?.updateColors(context)
    (getButton(DialogInterface.BUTTON_NEGATIVE) as? MaterialButton)?.updateColors(context)
}

fun BaseProgressIndicator<*>.updateColors(context: Context) {
    setIndicatorColor(getSecondaryColor(context))
    trackColor = getSecondaryContainerColor(context)
}

fun LoadingIndicator.updateColors(context: Context) {
    setIndicatorColor(getPrimaryColor(context))
}

fun CompoundButton.updateColors(context: Context) {
    val colorOnSurface = getColorOnSurface(context)
    val csl = ColorStateList(
        arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        ),
        intArrayOf(
            adjustColorAlpha(colorOnSurface, 0.38f),
            getPrimaryColor(context),
            colorOnSurface
        )
    )
    buttonTintList = csl
}

fun Menu.updateIconColors(context: Context, @ColorInt toolbarColor: Int) {
    val contentColor = if (isDynamicThemingEnabled(context)) {
        if (isNativeMonetAvailable()) {
            if (isColorLight(toolbarColor)) {
                context.getColor(android.R.color.system_accent2_900)
            } else {
                context.getColor(android.R.color.system_accent2_100)
            }
        } else {
            val monet = MonetCompat.getInstance()
            if (isColorLight(toolbarColor)) {
                monet.getMonetColors().accent2[900]!!.toArgb()
            } else {
                monet.getMonetColors().accent2[10]!!.toArgb()
            }
        }
    } else {
        context.getColor(com.google.android.material.R.color.m3_dark_default_color_primary_text)
    }

    forEach {
        it.icon?.run { mutate().colorFilter = BlendModeColorFilterCompat
            .createBlendModeColorFilterCompat(contentColor, BlendModeCompat.SRC_ATOP) }
    }
}

private fun MaterialButton.updateColors(context: Context) {
    rippleColor = getRippleColorForMaterialButton(context)
}

fun AppBarLayout.updateBackgroundColor(context: Context) {
    val backgroundColor = if (context.isNightMode()) {
        getSurfaceColor(context)
    } else {
        getPrimaryColor(context)
    }
    setBackgroundColor(backgroundColor)
}

fun MaterialToolbar.updateToolBarColor(context: Context) {
    if (isDynamicThemingEnabled(context)) {
        val backgroundColor = if (context.isNightMode()) {
            getSurfaceColor(context)
        } else {
            getPrimaryColor(context)
        }
        setBackgroundColor(backgroundColor)

        val contentColor = if (!isNativeMonetAvailable()) {
            val monet = MonetCompat.getInstance()
            if (isColorLight(backgroundColor)) {
                monet.getMonetColors().accent3[900]!!.toArgb()
            } else {
                monet.getMonetColors().accent3[10]!!.toArgb()
            }
        } else {
            if (isColorLight(backgroundColor)) {
                getColorOnSurface(context)
            } else {
                context.getColor(android.R.color.system_accent1_0)
            }
        }
        setTitleTextColor(contentColor)
        setNavigationIconTint(contentColor)
    } else {
        val backgroundColor: Int
        val contentColor = context.getColor(com.google.android.material.R.color.m3_dark_default_color_primary_text)

        if (context.isNightMode()) {
            backgroundColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, TAG)
        } else {
            backgroundColor = MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, TAG)
        }

        setBackgroundColor(backgroundColor)
        setTitleTextColor(contentColor)
        setNavigationIconTint(contentColor)
    }
}

fun NavigationRailView.updateColors(context: Context) {
    val backgroundColor: Int
    val activeTextColor: Int
    val inactiveColor: Int

    if (context.isNightMode()) {
        backgroundColor = getSurfaceColor(context)
        activeTextColor = if (isExpanded) getOnSecondaryContainerColor(context) else getColorOnSurface(context)
        inactiveColor = getColorOnSurfaceVariant(context)
    } else {
        backgroundColor = getPrimaryColor(context)
        val onPrimaryColor = getOnPrimaryColor(context)
        activeTextColor = if (isExpanded) getOnSecondaryContainerColor(context) else onPrimaryColor
        inactiveColor = onPrimaryColor
    }

    val indicatorColor = getSecondaryContainerColor(context)
    val activeIconColor = getOnSecondaryContainerColor(context)

    background.mutate().setTintList(ColorStateList.valueOf(backgroundColor))
    itemActiveIndicatorColor = ColorStateList.valueOf(indicatorColor)

    val itemTextColorStateList = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
        intArrayOf(activeTextColor, inactiveColor)
    )
    itemTextColor = itemTextColorStateList

    val itemIconColorStateList = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
        intArrayOf(activeIconColor, inactiveColor)
    )
    itemIconTintList = itemIconColorStateList

    itemRippleColor = getRippleColorForNavigationRail(context)
}

fun TabLayout.updateColors(context: Context) {
    val textColors = if (isDynamicThemingEnabled(context)) {
        ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(0)
            ),
            intArrayOf(
                getColorOnSurface(context),
                getPrimaryColor(context),
                getColorOnSurfaceVariant(context)
            )
        )
    } else {
        ContextCompat.getColorStateList(context, com.google.android.material.R.color.m3_tabs_icon_color)
    }

    setSelectedTabIndicatorColor(getPrimaryColor(context))
    tabIconTint = textColors
    tabTextColors = textColors
    tabRippleColor = getRippleColorForTabLayout(context)
    setBackgroundColor(getSurfaceColor(context))
}

@Suppress("DEPRECATION")
@SuppressLint("NewApi")
fun Window.updateStatusBarColor(context: Context) {
    val isDynamicTheming = isDynamicThemingEnabled(context)
    val color = if (isDynamicTheming) {
        if (context.isNightMode()) {
            getSurfaceColor(context)
        } else {
            getPrimaryColor(context)
        }
    } else {
        if (context.isNightMode()) {
            MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, javaClass.canonicalName)
        } else {
            MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, javaClass.canonicalName)
        }
    }

    if (Build.VERSION.SDK_INT >= 35) {
        WindowCompat.getInsetsController(this, decorView).isAppearanceLightStatusBars = isColorLight(color)
        return
    }

    statusBarColor = color

    if (isDynamicTheming) {
        val lightStatusBar = isColorLight(color)
        if (Build.VERSION.SDK_INT >= 31) {
            insetsController?.setSystemBarsAppearance(if (lightStatusBar)
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            val statusBarFlag = if (lightStatusBar) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
            decorView.systemUiVisibility =
                (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()) or statusBarFlag
        }
    }
}

private fun getRippleColorForMaterialButton(context: Context): ColorStateList? {
    return if (isDynamicThemingEnabled(context)) {
        val colorPrimary = getPrimaryColor(context)
        val colorOnSurface = getColorOnSurface(context)
        ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_hovered),
                intArrayOf(0),
            ),

            intArrayOf(
                adjustColorAlpha(colorPrimary, 0.12f),
                adjustColorAlpha(colorOnSurface, 0.12f),
                adjustColorAlpha(colorOnSurface, 0.08f),
                colorOnSurface
            )
        )
    } else {
        ContextCompat.getColorStateList(context, com.google.android.material.R.color.m3_text_button_ripple_color_selector)
    }
}

private fun getRippleColorForNavigationRail(context: Context): ColorStateList? {
    return if (isDynamicThemingEnabled(context)) {
        val colorPrimary = getPrimaryColor(context)
        val colorOnSurface = getColorOnSurface(context)
        ColorStateList(
            arrayOf(
                // selected
                intArrayOf(android.R.attr.state_pressed, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_focused, android.R.attr.state_hovered, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_focused, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_hovered, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_selected),

                // unselected
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused, android.R.attr.state_hovered),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_hovered),
                intArrayOf(0),
            ),

            intArrayOf(
                adjustColorAlpha(colorPrimary, 0.08f),
                adjustColorAlpha(colorPrimary, 0.16f),
                adjustColorAlpha(colorPrimary, 0.12f),
                adjustColorAlpha(colorPrimary, 0.04f),
                adjustColorAlpha(colorPrimary, 0.00f),

                adjustColorAlpha(colorOnSurface, 0.08f),
                adjustColorAlpha(colorOnSurface, 0.16f),
                adjustColorAlpha(colorOnSurface, 0.12f),
                adjustColorAlpha(colorOnSurface, 0.04f),
                adjustColorAlpha(colorOnSurface, 0.00f)
            )
        )
    } else {
        ContextCompat.getColorStateList(context, com.google.android.material.R.color.mtrl_navigation_bar_ripple_color)
    }
}

private fun getRippleColorForTabLayout(context: Context): ColorStateList? {
    return if (isDynamicThemingEnabled(context)) {
        val colorPrimary = getPrimaryColor(context)
        val colorOnSurfaceVariant = getColorOnSurfaceVariant(context)

        ColorStateList(
            arrayOf(
                // selected
                intArrayOf(android.R.attr.state_pressed, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_focused, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_hovered, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_selected),

                // unselected
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_hovered),
                intArrayOf(0),
            ),

            intArrayOf(
                adjustColorAlpha(colorPrimary, 0.12f),
                adjustColorAlpha(colorPrimary, 0.12f),
                adjustColorAlpha(colorPrimary, 0.08f),
                adjustColorAlpha(colorPrimary, 0.16f),

                adjustColorAlpha(colorOnSurfaceVariant, 0.12f),
                adjustColorAlpha(colorOnSurfaceVariant, 0.12f),
                adjustColorAlpha(colorOnSurfaceVariant, 0.08f),
                adjustColorAlpha(colorOnSurfaceVariant, 0.16f)
            )
        )
    } else {
        ContextCompat.getColorStateList(context, com.google.android.material.R.color.m3_tabs_ripple_color)
    }
}

fun MaterialAlertDialogBuilder.updateBackgroundColor(context: Context): MaterialAlertDialogBuilder {
    val materialShapeDrawable = MaterialShapeDrawable(
        context,
        null,
        androidx.appcompat.R.attr.alertDialogStyle,
        com.google.android.material.R.style.MaterialAlertDialog_Material3Expressive
    )
    materialShapeDrawable.initializeElevationOverlay(context)
    materialShapeDrawable.fillColor = ColorStateList.valueOf(getSurfaceColor(context))

    // dialogCornerRadius first appeared in Android Pie
    if (Build.VERSION.SDK_INT >= 28) {
        val dialogCornerRadiusValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.dialogCornerRadius, dialogCornerRadiusValue, true)
        val dialogCornerRadius =
            dialogCornerRadiusValue.getDimension(getContext().resources.displayMetrics)
        if (dialogCornerRadiusValue.type == TypedValue.TYPE_DIMENSION && dialogCornerRadius >= 0) {
            materialShapeDrawable.setCornerSize(dialogCornerRadius)
        }
    }

    return setBackground(materialShapeDrawable)
}

private fun adjustColorAlpha(@ColorInt color: Int, factor: Float): Int {
    val alpha = (Color.alpha(color) * factor).roundToInt()
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}

fun isColorLight(color: Int): Boolean =
    color != Color.TRANSPARENT && ColorUtils.calculateLuminance(color) > 0.5