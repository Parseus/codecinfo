@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.codecinfo.profilelevels

enum class AVCProfiles(val value: Int) {

    AVCProfileBaseline(0x01),
    AVCProfileMain(0x02),
    AVCProfileExtended(0x04),
    AVCProfileHigh(0x08),
    AVCProfileHigh10(0x10),
    AVCProfileHigh422(0x20),
    AVCProfileHigh444(0x40),
    AVCProfileHigh10Ultra(0x100),
    AVCProfileHigh422Ultra(0x200),
    AVCProfileHigh444Ultra(0x400),
    AVCProfileHighCAVLC444Ultra(0x800),
    AVCProfileHigh444Predictive(0x1000),
    AVCProfileScalableBaseline(0x2000),
    AVCProfileScalableHigh(0x4000),
    AVCProfileScalableHighIntra(0x8000),
    AVCProfileConstrainedBaseline(0x10000),
    AVCProfileConstrainedHigh(0x80000),

    AVCProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }
}

enum class AVCQualcommProfiles(val value: Int) {
    QOMX_VIDEO_AVCProfileConstrained(0x7F000000),
    QOMX_VIDEO_AVCProfileConstrainedBaseline(0x7F000001),
    QOMX_VIDEO_AVCProfileConstrainedHigh(0x7F000002);

    companion object {
        fun from(findValue: Int): String? = try {
            values().first { it.value == findValue }.name
        } catch (e: NoSuchElementException) {
            null
        }
    }
}

enum class AVCSamsungProfiles(val value: Int) {
    OMX_VIDEO_AVCProfileConstrainedBaseline(0x7F000001),
    OMX_VIDEO_AVCProfileConstrainedHigh(0x7F000002);

    companion object {
        fun from(findValue: Int): String? = try {
            values().first { it.value == findValue }.name
        } catch (e: NoSuchElementException) {
            null
        }
    }
}

enum class AVCLevels (val value: Int) {

    AVCLevel1(0x01),
    AVCLevel1b(0x02),
    AVCLevel11(0x04),
    AVCLevel12(0x08),
    AVCLevel13(0x10),
    AVCLevel2(0x20),
    AVCLevel21(0x40),
    AVCLevel22(0x80),
    AVCLevel3(0x100),
    AVCLevel31(0x200),
    AVCLevel32(0x400),
    AVCLevel4(0x800),
    AVCLevel41(0x1000),
    AVCLevel42(0x2000),
    AVCLevel5(0x4000),
    AVCLevel51(0x8000),
    AVCLevel52(0x10000),
    AVCLevel6(0x20000),
    AVCLevel61(0x40000),
    AVCLevel62(0x80000),
    AVCLevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = try {
            values().first { it.value == findValue }.name
        } catch (e: NoSuchElementException) {
            null
        }
    }
}