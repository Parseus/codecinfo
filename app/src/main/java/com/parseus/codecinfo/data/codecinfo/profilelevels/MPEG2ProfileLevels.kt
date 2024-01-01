@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class MPEG2Profiles(val value: Int) {

    MPEG2ProfileSimple(0x00),
    MPEG2ProfileMain(0x01),
    MPEG2Profile422(0x02),
    MPEG2ProfileSNR(0x03),
    MPEG2ProfileSpatial(0x04),
    MPEG2ProfileHigh(0x05),

    // Reneseas extensions
    OMF_MC_VIDEO_MPEG2ProfileMPEG1(0x7F000000),
    OMF_MC_VIDEO_MPEG2ProfileUnknown(0x7F000001),

    MPEG2ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int, extension: String): String? = entries.find {
            if (it.value >= 0x7F000000 && it.value != 0x7FFFFFFF) {
                it.value == findValue && it.name.contains(extension, true)
            } else {
                it.value == findValue
            }
        }?.name
    }

}

enum class MPEG2Levels(val value: Int) {
    MPEG2LevelLL(0x00),
    MPEG2LevelML(0x01),
    MPEG2LevelH14(0x02),
    MPEG2LevelHL(0x03),
    MPEG2LevelHP(0x04),

    // Reneseas extensions
    OMF_MC_VIDEO_MPEG2LevelMPEG1(0x7F000000),
    OMF_MC_VIDEO_MPEG2LevelUnknown(0x7F000001),

    MPEG2LevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int, extension: String): String? = entries.find {
            if (it.value >= 0x7F000000 && it.value != 0x7FFFFFFF) {
                it.value == findValue && it.name.contains(extension, true)
            } else {
                it.value == findValue
            }
        }?.name
    }

}