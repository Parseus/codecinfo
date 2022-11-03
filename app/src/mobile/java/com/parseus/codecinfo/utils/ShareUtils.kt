package com.parseus.codecinfo.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import java.util.*

fun getItemListString(context: Context): String {
    val builder = StringBuilder()

    if (InfoType.currentInfoType != InfoType.DRM) {
        builder.append("${context.getString(R.string.codec_list)}:\n\n")
        val codecSimpleInfoList = getSimpleCodecInfoList(context, true)
        codecSimpleInfoList.addAll(getSimpleCodecInfoList(context, false))
        codecSimpleInfoList.forEach { builder.append("$it\n") }
    } else if (Build.VERSION.SDK_INT >= 18) {
        builder.append("${context.getString(R.string.drm_list)}:\n\n")
        getSimpleDrmInfoList(context).forEach { builder.append("$it\n") }
    }

    return builder.toString()
}

fun getAllInfoString(context: Context): String {
    val builder = StringBuilder()

    builder.append("${context.getString(R.string.codec_list)}:\n")
    val codecSimpleInfoList = getSimpleCodecInfoList(context, true)
    codecSimpleInfoList.addAll(getSimpleCodecInfoList(context, false))

    for (info in codecSimpleInfoList) {
        builder.append("\n$info\n")
        getDetailedCodecInfo(context, info.codecId, info.codecName).forEach { builder.append("$it\n") }
    }

    if (Build.VERSION.SDK_INT >= 18) {
        builder.append("\n\n${context.getString(R.string.drm_list)}:\n")
        getSimpleDrmInfoList(context).forEach { infoItem ->
            builder.append("\n$infoItem\n")
            getDetailedDrmInfo(context, infoItem.drmUuid, DrmVendor.getFromUuid(infoItem.drmUuid)).forEach { builder.append("$it\n") }
        }
    }

    return builder.toString()
}

fun getSelectedCodecInfoString(context: Context, codecId: String, codecName: String): String {
    val builder = StringBuilder()
    builder.append("${context.getString(R.string.codec_details)}: $codecName\n\n")

    getDetailedCodecInfo(context, codecId, codecName).forEach { builder.append("$it\n") }

    return builder.toString()
}

@RequiresApi(18)
fun getSelectedDrmInfoString(context: Context, drmName: String, drmUuid: UUID): String {
    val builder = StringBuilder()
    builder.append("${context.getString(R.string.drm_details)}: $drmName\n\n")

    getDetailedDrmInfo(context, drmUuid, DrmVendor.getFromUuid(drmUuid)).forEach { builder.append("$it\n") }

    return builder.toString()
}