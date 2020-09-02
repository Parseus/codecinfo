@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.codecinfo.profilelevels

enum class AVSProfiles(val value: Int) {

    OMX_VIDEO_AVSProfileJizhun(0x00),
    OMX_VIDEO_AVSProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}

enum class AVSLevels (val value: Int) {

    OMX_VIDEO_AVSLevel20(0x00),
    OMX_VIDEO_AVSLevel40(0x01),
    OMX_VIDEO_AVSLevel42(0x02),
    OMX_VIDEO_AVSLevel60(0x03),
    OMX_VIDEO_AVSLevel62(0x04),
    OMX_VIDEO_AVSLevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }
}