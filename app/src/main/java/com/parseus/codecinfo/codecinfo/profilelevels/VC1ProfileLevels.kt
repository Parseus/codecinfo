@file:Suppress("EnumEntryName")

package com.parseus.codecinfo.codecinfo.profilelevels

enum class VC1Profiles(val value: Int) {

    QOMX_VIDEO_VC1ProfileSimple(0x01),
    QOMX_VIDEO_VC1ProfileMain(0x02),
    QOMX_VIDEO_VC1ProfileAdvanced(0x04),
    QOMX_VIDEO_VC1ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = try {
            VC1Profiles.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}

enum class VC1Levels(val value: Int) {

    QOMX_VIDEO_VC1LevelLow(0x01),
    QOMX_VIDEO_VC1LevelMedium(0x02),
    QOMX_VIDEO_VC1LevelHigh(0x04),
    QOMX_VIDEO_VC1Level0(0x08),
    QOMX_VIDEO_VC1Level1(0x10),
    QOMX_VIDEO_VC1Level2(0x20),
    QOMX_VIDEO_VC1Level3(0x40),
    QOMX_VIDEO_VC1Level4(0x80),
    QOMX_VIDEO_VC1LevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = try {
            VC1Levels.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}