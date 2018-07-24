package com.parseus.codecinfo.codecinfo.profilelevels

@Suppress("EnumEntryName")
enum class DivXProfiles(val value: Int) {

    QOMX_VIDEO_DivXProfileqMobile(0x01),
    QOMX_VIDEO_DivXProfileMobile(0x02),
    QOMX_VIDEO_DivXProfileMT(0x04),
    QOMX_VIDEO_DivXProfileHT(0x08),
    QOMX_VIDEO_DivXProfileHD(0x10),
    QOMX_VIDEO_DivXProfileFullHD(0x20),
    QOMX_VIDEO_DivXProfilePlusHD(0x40),
    QOMX_VIDEO_DivXProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int, extension: String = ""): String? = try {
            DivXProfiles.values().first {
                if (it.value > 0x7F000000 && it.value != 0x7FFFFFFF) {
                    it.value == findValue && it.name.contains(extension, true)
                } else {
                    it.value == findValue
                }
            }.name
        } catch (e: Exception) {
            null
        }
    }

}