package com.parseus.codecinfo.codecinfo.colorformats

import com.parseus.codecinfo.toHexHstring

@Suppress("EnumEntryName")
enum class SonyColorFormat(val value: Int) {

    OMX_STE_COLOR_FormatYUV420PackedSemiPlanarMB(0x7FA00000),
    OMX_COLOR_FormatYUV420MBPackedSemiPlanar(0x7FFFFFFE);

    companion object {
        fun from(findValue: Int) = SonyColorFormat.values().find { it.value == findValue }?.let {
            "${it.name} (${it.value.toHexHstring()})"
        }
    }

}