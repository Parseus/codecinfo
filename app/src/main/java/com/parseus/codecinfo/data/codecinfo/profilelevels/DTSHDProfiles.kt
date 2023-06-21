@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

@Suppress("EnumEntryName")
enum class DTSHDProfiles(val value: Int) {

    DTS_HDProfileHRA(0x1),
    DTS_HDProfileLBR(0x2),
    DTS_HDProfileMA(0x4),
    DTS_HDProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}