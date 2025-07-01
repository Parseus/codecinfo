@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

@Suppress("EnumEntryName")
enum class IAMFProfiles(val value: Int) {

    IAMFProfileSimpleOpus(0x01010001),
    IAMFProfileSimpleAac(0x01010002),
    IAMFProfileSimpleFlac(0x01010004),
    IAMFProfileSimplePcm(0x01010008),
    IAMFProfileBaseOpus(0x01020001),
    IAMFProfileBaseAac(0x01020002),
    IAMFProfileBaseFlac(0x01020004),
    IAMFProfileBasePcm(0x01020008),
    IAMFProfileBaseEnhancedOpus(0x01040001),
    IAMFProfileBaseEnhancedAac(0x01040002),
    IAMFProfileBaseEnhancedFlac(0x01040004),
    IAMFProfileBaseEnhancedPcm(0x01040008);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}