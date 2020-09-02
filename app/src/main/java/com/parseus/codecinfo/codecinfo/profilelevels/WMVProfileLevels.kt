@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.codecinfo.profilelevels

enum class WMVProfiles(val value: Int) {

    OMX_VIDEO_WMVProfileSimple(0x00),
    OMX_VIDEO_WMVProfileMain(0x01),
    OMX_VIDEO_WMVProfileAdvanced(0x02),
    OMX_VIDEO_WMVProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}

enum class WMVLevels(val value: Int) {

    OMX_VIDEO_WMVLevelLow(0x00),
    OMX_VIDEO_WMVLevelMedium(0x01),
    OMX_VIDEO_WMVLevelHigh(0x02),
    OMX_VIDEO_WMVl0(0x03),
    OMX_VIDEO_WMVL1(0x04),
    OMX_VIDEO_WMVL2(0x05),
    OMX_VIDEO_WMVL3(0x06),
    OMX_VIDEO_WMVL4(0x07),
    OMX_VIDEO_WMVLevelSMPTEReserved(0x08),
    OMX_VIDEO_WMVLevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}