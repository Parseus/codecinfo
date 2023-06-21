@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

@Suppress("EnumEntryName")
enum class DTSUHDProfiles(val value: Int) {

    DTS_UHDProfileP1(0x1),
    DTS_UHDProfileP2(0x2),
    DTS_UDHProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}