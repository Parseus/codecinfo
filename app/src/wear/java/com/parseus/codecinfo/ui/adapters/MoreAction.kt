package com.parseus.codecinfo.ui.adapters

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MoreAction(
    val id: Int,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int
)