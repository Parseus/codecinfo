package com.parseus.codecinfo.codecinfo.profilelevels

enum class MPEG2Profiles(val value: Int) {

    MPEG2ProfileSimple(0x00),
    MPEG2ProfileMain(0x01),
    MPEG2Profile422(0x02),
    MPEG2ProfileSNR(0x03),
    MPEG2ProfileSpatial(0x04),
    MPEG2ProfileHigh(0x05),
    MPEG2ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = try {
            MPEG2Profiles.values().first { it.value == findValue }.name
        } catch (e: NoSuchElementException) {
            null
        }
    }

}

enum class MPEG2Levels(val value: Int) {
    MPEG2LevelLL(0x00),
    MPEG2LevelML(0x01),
    MPEG2LevelH14(0x02),
    MPEG2LevelHL(0x03),
    MPEG2LevelHP(0x04),
    MPEG2LevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = try {
            MPEG2Levels.values().first { it.value == findValue }.name
        } catch (e: NoSuchElementException) {
            null
        }
    }

}