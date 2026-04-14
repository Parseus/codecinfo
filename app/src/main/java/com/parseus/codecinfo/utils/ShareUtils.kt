package com.parseus.codecinfo.utils

import android.content.Context
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import java.util.*

fun getItemListString(context: Context): String = buildString {
    val infoType = InfoType.currentInfoType
    val isDrm = infoType == InfoType.DRM

    val titleRes = if (isDrm) R.string.drm_list else R.string.codec_list
    append(context.getString(titleRes), ":\n\n")

    if (isDrm) {
        getSimpleDrmInfoList(context).joinTo(this, separator = "\n", postfix = "\n")
    } else {
        // Use + to create a new list instead of addAll to avoid mutating the cache
        val allCodecs = getSimpleCodecInfoList(context, true) + getSimpleCodecInfoList(context, false)
        allCodecs.joinTo(this, separator = "\n", postfix = "\n")
    }
}

fun getAllInfoString(context: Context): String = buildString {
    appendLine("${context.getString(R.string.codec_list)}:")

    // Use + to create a new list instead of addAll to avoid mutating the internal cache
    val codecSimpleInfoList = getSimpleCodecInfoList(context, true) + getSimpleCodecInfoList(context, false)

    for (info in codecSimpleInfoList) {
        appendLine("\n$info")
        getDetailedCodecInfo(context, info.codecId, info.codecName).forEach { appendLine(it) }
    }

    appendLine("\n\n${context.getString(R.string.drm_list)}:")
    getSimpleDrmInfoList(context).forEach { infoItem ->
        appendLine("\n$infoItem")
        val drmVendor = DrmVendor.getFromUuid(infoItem.drmUuid)
        getDetailedDrmInfo(context, infoItem.drmUuid, drmVendor).forEach { appendLine(it) }
    }
}

fun getSelectedCodecInfoString(context: Context, codecId: String, codecName: String): String = buildString {
    appendLine("${context.getString(R.string.codec_details)}: $codecName\n")
    getDetailedCodecInfo(context, codecId, codecName).forEach { appendLine(it) }
}

fun getSelectedDrmInfoString(context: Context, drmName: String, drmUuid: UUID): String = buildString {
    appendLine("${context.getString(R.string.drm_details)}: $drmName\n")
    getDetailedDrmInfo(context, drmUuid, DrmVendor.getFromUuid(drmUuid)).forEach { appendLine(it) }
}