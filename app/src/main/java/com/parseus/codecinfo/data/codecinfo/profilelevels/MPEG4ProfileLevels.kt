@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class MPEG4Profiles(val value: Int) {

    MPEG4ProfileSimple(0x01),
    MPEG4ProfileSimpleScalable(0x02),
    MPEG4ProfileCore(0x04),
    MPEG4ProfileMain(0x08),
    MPEG4ProfileNbit(0x10),
    MPEG4ProfileScalableTexture(0x20),
    MPEG4ProfileSimpleFace(0x40),
    MPEG4ProfileSimpleFBA(0x80),
    MPEG4ProfileBasicAnimated(0x100),
    MPEG4ProfileHybrid(0x200),
    MPEG4ProfileAdvancedRealTime(0x400),
    MPEG4ProfileCoreScalable(0x800),
    MPEG4ProfileAdvancedCoding(0x1000),
    MPEG4ProfileAdvancedCore(0x2000),
    MPEG4ProfileAdvancedScalable(0x4000),
    MPEG4ProfileAdvancedSimple(0x8000),
    MPEG4ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}

enum class MPEG4Levels(val value: Int) {

    MPEG4Level0(0x01),
    MPEG4Level0b(0x02),
    MPEG4Level1(0x04),
    MPEG4Level2(0x08),
    MPEG4Level3(0x10),
    MPEG4Level3b(0x18),
    MPEG4Level4(0x20),
    MPEG4Level4a(0x40),
    MPEG4Level5(0x80),
    MPEG4Level6(0x100),

    // Qualcomm extensions
    QOMX_VIDEO_MPEG4Level6(0x7F000001),
    QOMX_VIDEO_MPEG4Level7(0x7F000002),
    QOMX_VIDEO_MPEG4Level8(0x7F000003),
    QOMX_VIDEO_MPEG4Level9(0x7F000004),

    // Samsung extensions
    OMX_SEC_VIDEO_MPEG4Level6(0x7F000001),
    OMX_SEC_VIDEO_MPEG4Level7(0x7F000002),
    OMX_SEC_VIDEO_MPEG4Level8(0x7F000003),
    OMX_SEC_VIDEO_MPEG4Level9(0x7F000004),

    // Reneseas extensions
    OMF_MC_VIDEO_MPEG4Level3b(0x7F000000),
    OMF_MC_VIDEO_MPEG4Level6(0x7F000001),
    OMF_MC_VIDEO_MPEG4LevelNone(0x7F000002),
    OMF_MC_VIDEO_MPEG4LevelUnknown(0x7F000003),

    MPEG4LevelMax(0x7FFFFFFF);

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