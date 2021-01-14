package com.parseus.codecinfo

import android.content.Context
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.drm.DrmVendor
import com.parseus.codecinfo.drm.getDetailedDrmInfo
import com.parseus.codecinfo.drm.getSimpleDrmInfoList
import java.util.*

fun getItemListString(context: Context): String {
    val builder = StringBuilder()

    if (InfoType.currentInfoType != InfoType.DRM) {
        builder.append("${context.getString(R.string.codec_list)}:\n\n")
        val codecSimpleInfoList = getSimpleCodecInfoList(context, true)
        codecSimpleInfoList.addAll(getSimpleCodecInfoList(context, false))
        codecSimpleInfoList.forEach { builder.append("$it\n") }
    } else {
        builder.append("${context.getString(R.string.drm_list)}:\n\n")
        getSimpleDrmInfoList().forEach { builder.append("$it\n") }
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
        getDetailedCodecInfo(context, info.codecId, info.codecName).forEach { (key, value) ->
            val stringToAppend = if (key != context.getString(R.string.bitrate_modes)
                    && key != context.getString(R.string.color_profiles)
                    && key != context.getString(R.string.profile_levels)
                    && key != context.getString(R.string.max_frame_rate_per_resolution)) {
                "$key: $value\n"
            } else {
                "$key:\n$value\n"
            }
            builder.append(stringToAppend)
        }
    }

    builder.append("\n\n${context.getString(R.string.drm_list)}:\n")
    getSimpleDrmInfoList().forEach { infoItem ->
        builder.append("\n$infoItem\n")
        getDetailedDrmInfo(context, DrmVendor.getFromUuid(infoItem.drmUuid)).forEach { (key, value) ->
            builder.append("$key: $value\n")
        }
    }

    return builder.toString()
}

fun getSelectedCodecInfoString(context: Context, codecId: String, codecName: String): String {
    val builder = StringBuilder()
    builder.append("${context.getString(R.string.codec_details)}: $codecName\n\n")

    getDetailedCodecInfo(context, codecId, codecName).forEach { (key, value) ->
        val stringToAppend = if (key != context.getString(R.string.bitrate_modes)
                && key != context.getString(R.string.color_profiles)
                && key != context.getString(R.string.profile_levels)
                && key != context.getString(R.string.max_frame_rate_per_resolution)) {
            "$key: $value\n"
        } else {
            "$key:\n$value\n"
        }
        builder.append(stringToAppend)
    }

    return builder.toString()
}

fun getSelectedDrmInfoString(context: Context, drmName: String, drmUuid: UUID): String {
    val builder = StringBuilder()
    builder.append("${context.getString(R.string.drm_details)}: $drmName\n\n")

    getDetailedDrmInfo(context, DrmVendor.getFromUuid(drmUuid)).forEach { (key, value) ->
        builder.append("$key: $value\n")
    }

    return builder.toString()
}