package com.parseus.codecinfo.utils

import android.content.Context
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList

fun getCodecAndDrmItemListString(context: Context): String {
    val builder = StringBuilder()

    builder.append("${context.getString(R.string.codec_list)}:\n\n")
    val codecSimpleInfoList = getSimpleCodecInfoList(context, true)
    codecSimpleInfoList.addAll(getSimpleCodecInfoList(context, false))
    codecSimpleInfoList.forEach { builder.append("$it\n") }

    builder.append("\n\n${context.getString(R.string.drm_list)}:\n\n")
    getSimpleDrmInfoList(context).forEach { builder.append("$it\n") }

    return builder.toString()
}