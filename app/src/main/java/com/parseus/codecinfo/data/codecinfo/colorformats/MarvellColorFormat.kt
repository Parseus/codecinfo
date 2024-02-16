@file:Suppress("unused", "EnumEntryName")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class MarvellColorFormat(val value: Int) {

    OMX_COLOR_FormatYV12(0x7F000001),
    OMX_COLOR_Format15bitRGB555(0x7F000002),
    OMX_COLOR_Format16bitBGR555(0x7F000003),
    OMX_COLOR_FormatGRAY8(0x7F000004),
    OMX_COLOR_FormatYUV444Planar(0x7F000005),
    OMX_COLOR_FormatIppPicture(0x7F000006);

    companion object {
        fun from(findValue: Int) = entries.find { it.value == findValue }?.name
    }

}