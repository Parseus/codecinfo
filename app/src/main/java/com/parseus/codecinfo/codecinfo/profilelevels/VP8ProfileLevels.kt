package com.parseus.codecinfo.codecinfo.profilelevels

enum class VP8Profiles(val value: Int) {

    VP8ProfileMain(0x01);

    companion object {
        fun from(findValue: Int): String? = try {
            VP8Profiles.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}

@Suppress("EnumEntryName")
enum class VP8Levels(val value: Int) {

    VP8Level_Version0(0x01),
    VP8Level_Version1(0x02),
    VP8Level_Version2(0x04),
    VP8Level_Version3(0x08);

    companion object {
        fun from(findValue: Int): String? = try {
            VP8Levels.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}