@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class RenesasColorFormat(val value: Int) {

    OMF_MC_COLOR_FormatYUV411HV22(0x13),
    OMF_MC_COLOR_FormatYUV411HV41(0x7F000000),
    OMF_MC_COLOR_FormatYUV422HV21(0x7F000001),
    OMF_MC_COLOR_FormatYUV422HV12(0x7F000002),
    OMF_MC_COLOR_FormatYUV444HV11(0x7F000003);

    companion object {
        fun from(findValue: Int) = entries.find { it.value == findValue }?.name
    }

}