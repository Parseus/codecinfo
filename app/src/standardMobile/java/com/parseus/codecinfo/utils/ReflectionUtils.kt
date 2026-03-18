package com.parseus.codecinfo.utils

import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass
import org.lsposed.hiddenapibypass.LSPass

const val CAN_USE_REFLECTION_FOR_MCAPABILITIESINFO = true

fun disableApiBlacklistOnPie() {
    try {
        val signature = "Landroid/media/MediaCodecInfo\$CodecCapabilities"

        if (Build.VERSION.SDK_INT >= 36) {
            // LSPass will be blocked if core platform API restriction is enabled,
            // so use HiddenApiBypass to be safe.
            HiddenApiBypass.addHiddenApiExemptions(signature)
        } else if (Build.VERSION.SDK_INT >= 28) {
            val result = LSPass.addHiddenApiExemptions(signature)
            if (!result) {
                HiddenApiBypass.addHiddenApiExemptions(signature)
            }
        }
    } catch (_: Throwable) {}
}