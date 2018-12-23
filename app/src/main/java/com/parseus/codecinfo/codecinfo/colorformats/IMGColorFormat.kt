package com.parseus.codecinfo.codecinfo.colorformats

import com.parseus.codecinfo.toHexHstring

@Suppress("EnumEntryName")
enum class IMGColorFormat(val value: Int) {

    OMX_COLOR_FormatYVU420SemiPlanar(0x7F000001),
    OMX_COLOR_FormatYVU420PackedSemiPlanar(0x7F000002),
    OMX_COLOR_FormatYUV444Planar(0x7F000003),
    OMX_COLOR_FormatCMYK(0x7F000004);

    companion object {
        fun from(findValue: Int) = IMGColorFormat.values().find { it.value == findValue }?.let {
            "${it.name} (${it.value.toHexHstring()})"
        }
    }

}