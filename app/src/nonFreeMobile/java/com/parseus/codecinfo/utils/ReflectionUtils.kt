package com.parseus.codecinfo.utils

import android.os.Build

val CAN_USE_REFLECTION_FOR_MCAPABILITIESINFO = Build.VERSION.SDK_INT < 28
fun disableApiBlacklistOnPie() {}