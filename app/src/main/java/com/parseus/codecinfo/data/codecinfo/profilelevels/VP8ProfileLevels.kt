@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class VP8Profiles(val value: Int) {

    VP8ProfileMain(0x01),
    VP8ProfileAdvanced(0x02),
    VP8ProfileVersion0(0x04),
    VP8ProfileVersion1(0x08),
    VP8ProfileVersion2(0x10),
    VP8ProfileVersion3(0x20),
    VP8ProfileUnknown(0x6EFFFFFF),
    VP8ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}

@Suppress("EnumEntryName")
enum class VP8Levels(val value: Int) {

    VP8Level_Version0(0x01),
    VP8Level_Version1(0x02),
    VP8Level_Version2(0x04),
    VP8Level_Version3(0x08),
    VP8Level_Unknown(0x6EFFFFFF),
    VP8Level_Max(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}