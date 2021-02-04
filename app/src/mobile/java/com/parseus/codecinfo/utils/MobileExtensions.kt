package com.parseus.codecinfo.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.parseus.codecinfo.R

fun Context.isInTwoPaneMode(): Boolean {
    return resources.getBoolean(R.bool.twoPaneMode)
}

fun Context.getAttributeColor(@AttrRes attrColor: Int,
                              typedValue: TypedValue = TypedValue(),
                              resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}