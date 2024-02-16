@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class VC1Profiles(val value: Int) {

    OMX_VIDEO_VC1ProfileUnused(0x00),
    OMX_VIDEO_VC1ProfileSimple(0x01),
    OMX_VIDEO_VC1ProfileMain(0x02),
    OMX_VIDEO_VC1ProfileAdvanced(0x03),
    WMV9ProfileSimple(0x08),
    WMV9ProfileMain(0x10),
    WMV9ProfileComplex(0x20),
    OMX_VIDEO_VC1ProfileMax(0x7F000001),

    // Renesas extensions
    OMF_MC_VIDEO_VC1ProfileSimple(0x01),
    OMF_MC_VIDEO_VC1ProfileMain(0x02),
    OMF_MC_VIDEO_VC1ProfileAdvanced(0x04);

    companion object {
        fun from(findValue: Int, extension: String?): String? = entries.find {
            if (extension != null) {
                it.value == findValue && it.name.contains(extension, true)
            } else {
                it.value == findValue
            }
        }?.name
    }

}

enum class VC1Levels(val value: Int) {

    OMX_VIDEO_VC1LevelUnused(0x00),
    OMX_VIDEO_VC1LevelLow(0x01),
    OMX_VIDEO_VC1LevelMedium(0x02),
    OMX_VIDEO_VC1LevelHigh(0x03),
    OMX_VIDEO_VC1Level0(0x04),
    OMX_VIDEO_VC1Level1(0x05),
    OMX_VIDEO_VC1Level2(0x06),
    OMX_VIDEO_VC1Level3(0x07),
    OMX_VIDEO_VC1Level4(0x08),
    OMX_VIDEO_VC1LevelSMPTEReserved(0x09),
    OMX_VIDEO_VC1LevelMax(0x7F000001),

    // Renesas extensions
    OMF_MC_VIDEO_VC1LevelLow(0x01),
    OMF_MC_VIDEO_VC1LevelMedium(0x02),
    OMF_MC_VIDEO_VC1LevelHigh(0x04),
    OMF_MC_VIDEO_VC1Level0(0x08),
    OMF_MC_VIDEO_VC1Level1(0x10),
    OMF_MC_VIDEO_VC1Level2(0x20),
    OMF_MC_VIDEO_VC1Level3(0x40),
    OMF_MC_VIDEO_VC1LevelUnknown(0x7F000000);

    companion object {
        fun from(findValue: Int, extension: String?): String? = entries.find {
            if (extension != null) {
            it.value == findValue && it.name.contains(extension, true)
            } else {
            it.value == findValue
            }
        }?.name
    }

}