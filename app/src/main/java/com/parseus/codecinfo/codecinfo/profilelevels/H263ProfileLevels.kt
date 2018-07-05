package com.parseus.codecinfo.codecinfo.profilelevels

enum class H263Profiles(val value: Int) {

    H263ProfileBaseline(0x01),
    H263ProfileH320Coding(0x02),
    H263ProfileBackwardCompatible(0x04),
    H263ProfileISWV2(0x08),
    H263ProfileISWV3(0x10),
    H263ProfileHighCompression(0x20),
    H263ProfileInternet(0x40),
    H263ProfileInterlace(0x80),
    H263ProfileHighLatency(0x100);

    companion object {
        fun from(findValue: Int): String? = try {
            H263Profiles.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}

enum class H263Levels(val value: Int) {

    H263Level10(0x01),
    H263Level20(0x02),
    H263Level30(0x04),
    H263Level40(0x08),
    H263Level45(0x10),
    H263Level50(0x20),
    H263Level60(0x40),
    H263Level70(0x80);

    companion object {
        fun from(findValue: Int): String? = try {
            H263Levels.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}