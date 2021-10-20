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
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayout
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.toArgb
import com.parseus.codecinfo.R
import kotlin.math.roundToInt

fun isNativeMonetAvailable(): Boolean = Build.VERSION.SDK_INT >= 31
        && "Google" == Build.MANUFACTURER && Build.MODEL.startsWith("Pixel")

fun isDynamicThemingEnabled(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dynamic_theme", false)
}

fun getPrimaryColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
        val monet = MonetCompat.getInstance()
        if (context.isNightMode()) {
            monet.getMonetColors().accent1[200]!!.toArgb()
        } else {
            monet.getMonetColors().accent1[600]!!.toArgb()
        }
    } else {
        context.getAttributeColor(com.google.android.material.R.attr.colorPrimary)
    }
}

fun getSecondaryColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
        val monet = MonetCompat.getInstance()
        if (context.isNightMode()) {
            monet.getMonetColors().accent3[200]!!.toArgb()
        } else {
            monet.getMonetColors().accent3[600]!!.toArgb()
        }
    } else {
        context.getAttributeColor(com.google.android.material.R.attr.colorSecondary)
    }
}

fun getSurfaceColor(context: Context): Int {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
        MonetCompat.getInstance().getBackgroundColor(context)
    } else {
        context.getAttributeColor(com.google.android.material.R.attr.colorSurface)
    }
}

fun getColorOnSurface(context: Context): Int {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
        val monet = MonetCompat.getInstance()
        if (context.isNightMode()) {
            monet.getMonetColors().neutral1[100]!!.toArgb()
        } else {
            monet.getMonetColors().neutral1[900]!!.toArgb()
        }
    } else {
        context.getAttributeColor(com.google.android.material.R.attr.colorOnSurface)
    }
}

fun getColorOnSurfaceVariant(context: Context): Int {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
        val monet = MonetCompat.getInstance()
        if (context.isNightMode()) {
            monet.getMonetColors().neutral2[200]!!.toArgb()
        } else {
            monet.getMonetColors().neutral2[700]!!.toArgb()
        }
    } else {
        context.getAttributeColor(com.google.android.material.R.attr.colorSurfaceVariant)
    }
}

fun AlertDialog.updateButtonColors(context: Context) {
    (getButton(DialogInterface.BUTTON_POSITIVE) as? MaterialButton)?.updateColors(context)
    (getButton(DialogInterface.BUTTON_NEUTRAL) as? MaterialButton)?.updateColors(context)
    (getButton(DialogInterface.BUTTON_NEGATIVE) as? MaterialButton)?.updateColors(context)
}

fun CompoundButton.updateColors(context: Context) {
    if (Build.VERSION.SDK_INT >= 21) {
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
}

fun Menu.updateIconColors(context: Context, @ColorInt toolbarColor: Int) {
    if (isDynamicThemingEnabled(context)) {
        if (!isNativeMonetAvailable()) {
            val monet = MonetCompat.getInstance()
            val contentColor = if (isColorLight(toolbarColor)) {
                monet.getMonetColors().accent2[900]!!.toArgb()
            } else {
                monet.getMonetColors().accent2[10]!!.toArgb()
            }

            val searchView = findItem(R.id.menu_item_search).actionView as SearchView
            val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
            searchIcon.setColorFilter(contentColor)

            forEach {
                it.icon?.run { mutate().colorFilter = BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(contentColor, BlendModeCompat.SRC_ATOP) }
            }
        }
    } else {
        val contentColor = context.getAttributeColor(com.google.android.material.R.attr.colorControlNormal)
        val settingsColor = ContextCompat.getColor(context, R.color.white)

        val searchView = findItem(R.id.menu_item_search).actionView as SearchView
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        searchIcon.setColorFilter(contentColor)

        forEach {
            if (it.itemId == R.id.menu_item_settings) {
                it.icon?.run { mutate().colorFilter = BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(settingsColor, BlendModeCompat.SRC_ATOP) }
            } else {
                it.icon?.run { mutate().colorFilter = BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(contentColor, BlendModeCompat.SRC_ATOP) }
            }
        }
    }
}

private fun MaterialButton.updateColors(context: Context) {
    rippleColor = getRippleColorForMaterialButton(context)
}

fun MaterialToolbar.updateToolBarColor(context: Context) {
    if (isDynamicThemingEnabled(context)) {
        if (!isNativeMonetAvailable()) {
            val monet = MonetCompat.getInstance()
            val backgroundColor = if (context.isNightMode()) {
                monet.getBackgroundColor(context, true)
            } else {
                getPrimaryColor(context)
            }
            setBackgroundColor(backgroundColor)
            val contentColor = if (isColorLight(backgroundColor)) {
                monet.getMonetColors().accent3[900]!!.toArgb()
            } else {
                monet.getMonetColors().accent3[10]!!.toArgb()
            }
            setTitleTextColor(contentColor)
            setNavigationIconTint(contentColor)
        }
    } else {
        if (context.isNightMode()) {
            setBackgroundColor(context.getAttributeColor(com.google.android.material.R.attr.colorSurface))
        } else {
            setBackgroundColor(context.getAttributeColor(com.google.android.material.R.attr.colorPrimary))
        }
        setTitleTextColor(ContextCompat.getColor(context,
            com.google.android.material.R.color.m3_dark_default_color_primary_text))
    }
}

fun NavigationRailView.updateColors(context: Context) {
    if (isDynamicThemingEnabled(context)) {
        if (!isNativeMonetAvailable()) {
            val monet = MonetCompat.getInstance()
            val backgroundColor = if (context.isNightMode()) {
                monet.getBackgroundColor(context, true)
            } else {
                getPrimaryColor(context)
            }
            DrawableCompat.setTintList(background.mutate(), ColorStateList.valueOf(backgroundColor))

            val indicatorColor = if (context.isNightMode()) {
                monet.getMonetColors().accent3[700]!!.toArgb()
            } else {
                monet.getMonetColors().accent3[100]!!.toArgb()
            }

            itemActiveIndicatorColor = ColorStateList.valueOf(indicatorColor)

            val activeColor = if (!isColorLight(backgroundColor)) {
                monet.getMonetColors().neutral2[200]!!.toArgb()
            } else {
                monet.getMonetColors().neutral2[700]!!.toArgb()
            }
            val inactiveColor = if (!isColorLight(backgroundColor)) {
                monet.getMonetColors().neutral1[100]!!.toArgb()
            } else {
                monet.getMonetColors().neutral1[900]!!.toArgb()
            }

            val itemTextColorStateList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
                intArrayOf(inactiveColor, activeColor)
            )
            itemTextColor = itemTextColorStateList
        }
    } else {
        val backgroundColor = if (context.isNightMode()) {
            context.getAttributeColor(com.google.android.material.R.attr.colorSurface)
        } else {
            context.getAttributeColor(com.google.android.material.R.attr.colorPrimary)
        }
        DrawableCompat.setTintList(background.mutate(), ColorStateList.valueOf(backgroundColor))
        val indicatorColor = context.getAttributeColor(com.google.android.material.R.attr.colorSecondaryContainer)
        itemActiveIndicatorColor = ColorStateList.valueOf(indicatorColor)

        val activeColor = ContextCompat.getColor(context, com.google.android.material.R.color.m3_ref_palette_neutral95)
        val inactiveColor = ContextCompat.getColor(context, com.google.android.material.R.color.m3_ref_palette_neutral70)
        val itemTextColorStateList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
            intArrayOf(activeColor, inactiveColor)
        )
        itemTextColor = itemTextColorStateList
    }

    itemRippleColor = getRippleColorForNavigationRail(context)
}

fun TabLayout.updateColors(context: Context) {
    val textColors = if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
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
        AppCompatResources.getColorStateList(context, com.google.android.material.R.color.m3_tabs_icon_color)
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
    if (isDynamicThemingEnabled(context)) {
        val color = if (isNativeMonetAvailable()) {
            ContextCompat.getColor(context, android.R.color.system_accent1_700)
        } else {
            val monet = MonetCompat.getInstance()
            monet.getMonetColors().accent1[700]!!.toArgb()
        }
        statusBarColor = color

        val lightStatusBar = isColorLight(color)
        if (Build.VERSION.SDK_INT >= 31) {
            insetsController?.setSystemBarsAppearance(if (lightStatusBar)
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else if (Build.VERSION.SDK_INT >= 23) {
            val statusBarFlag = if (lightStatusBar) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
            decorView.systemUiVisibility =
                (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()) or statusBarFlag
        }
    } else if (Build.VERSION.SDK_INT >= 21) {
        statusBarColor = context.getAttributeColor(com.google.android.material.R.attr.colorPrimaryVariant)
    }
}

private fun getRippleColorForMaterialButton(context: Context): ColorStateList? {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
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
        AppCompatResources.getColorStateList(context, com.google.android.material.R.color.mtrl_navigation_bar_ripple_color)
    }
}

private fun getRippleColorForNavigationRail(context: Context): ColorStateList? {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
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
        AppCompatResources.getColorStateList(context, com.google.android.material.R.color.mtrl_navigation_bar_ripple_color)
    }
}

private fun getRippleColorForTabLayout(context: Context): ColorStateList? {
    return if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
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
        AppCompatResources.getColorStateList(context, com.google.android.material.R.color.m3_tabs_ripple_color)
    }
}

fun MaterialAlertDialogBuilder.updateBackgroundColor(context: Context): MaterialAlertDialogBuilder {
    val surfaceColor = if (isDynamicThemingEnabled(context) && !isNativeMonetAvailable()) {
        getSurfaceColor(context)
    } else {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, javaClass.canonicalName)
    }
    val materialShapeDrawable = MaterialShapeDrawable(
        context,
        null,
        com.google.android.material.R.attr.alertDialogStyle,
        com.google.android.material.R.style.MaterialAlertDialog_MaterialComponents
    )
    materialShapeDrawable.initializeElevationOverlay(context)
    materialShapeDrawable.fillColor = ColorStateList.valueOf(surfaceColor)

    // dialogCornerRadius first appeared in Android Pie

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

private fun isColorLight(color: Int): Boolean =
    color != Color.TRANSPARENT && ColorUtils.calculateLuminance(color) > 0.5