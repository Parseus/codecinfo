@file:Suppress("unused", "EnumEntryName")

package com.parseus.codecinfo.codecinfo.colorformats

enum class IMGColorFormat(val value: Int) {

    OMX_COLOR_FormatYVU420SemiPlanar(0x7F000001),
    OMX_COLOR_FormatYVU420PackedSemiPlanar(0x7F000002),
    OMX_COLOR_FormatYUV444Planar(0x7F000003),
    OMX_COLOR_FormatCMYK(0x7F000004);

    companion object {
        fun from(findValue: Int) = values().find { it.value == findValue }?.name
    }

}