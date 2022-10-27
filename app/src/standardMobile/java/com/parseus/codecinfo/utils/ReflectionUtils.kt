package com.parseus.codecinfo.utils

import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass

const val CAN_USE_REFLECTION_FOR_MCAPABILITIESINFO = true

fun disableApiBlacklistOnPie() {
    if (Build.VERSION.SDK_INT >= 28) {
        HiddenApiBypass.addHiddenApiExemptions("Landroid/media/MediaCodecInfo\$CodecCapabilities");
    }
}