package com.parseus.codecinfo.utils

import android.content.Context
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList

suspend fun getCodecAndDrmItemListString(context: Context): String = buildString {
    append(context.getString(R.string.codec_list)).append(":\n\n")

    val audioCodecs = getSimpleCodecInfoList(context, true)
    val videoCodecs = getSimpleCodecInfoList(context, false)

    audioCodecs.joinTo(this, separator = "\n", postfix = "\n")
    videoCodecs.joinTo(this, separator = "\n", postfix = "\n")

    append("\n\n").append(context.getString(R.string.drm_list)).append(":\n\n")
    getSimpleDrmInfoList(context).joinTo(this, separator = "\n", postfix = "\n")
}