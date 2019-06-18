package com.parseus.codecinfo.codecinfo.profilelevels

@Suppress("EnumEntryName")
enum class VP6Profiles(val value: Int) {

    OMX_VIDEO_VP6ProfileSimple(0x01),
    OMX_VIDEO_VP6ProfileAdvanced(0x02),
    OMX_VIDEO_VP6ProfileHeightenedSharpness(0x03),
    OMX_VIDEO_VP6ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}