package com.parseus.codecinfo.codecinfo.profilelevels

enum class HEVCProfiles(val value: Int) {

    HEVCProfileMain(0x01),
    HEVCProfileMain10(0x02),
    HEVCProfileMain10HDR10(0x1000);

    companion object {
        fun from(findValue: Int): String? = try {
            HEVCProfiles.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}

enum class HEVCLevels(val value: Int) {

    HEVCMainTierLevel1(0x1),
    HEVCHighTierLevel1(0x2),
    HEVCMainTierLevel2(0x4),
    HEVCHighTierLevel2(0x8),
    HEVCMainTierLevel21(0x10),
    HEVCHighTierLevel21(0x20),
    HEVCMainTierLevel3(0x40),
    HEVCHighTierLevel3(0x80),
    HEVCMainTierLevel31(0x100),
    HEVCHighTierLevel31(0x200),
    HEVCMainTierLevel4(0x400),
    HEVCHighTierLevel4(0x800),
    HEVCMainTierLevel41(0x1000),
    HEVCHighTierLevel41(0x2000),
    HEVCMainTierLevel5(0x4000),
    HEVCHighTierLevel5(0x8000),
    HEVCMainTierLevel51(0x10000),
    HEVCHighTierLevel51(0x20000),
    HEVCMainTierLevel52(0x40000),
    HEVCHighTierLevel52(0x80000),
    HEVCMainTierLevel6(0x100000),
    HEVCHighTierLevel6(0x200000),
    HEVCMainTierLevel61(0x400000),
    HEVCHighTierLevel61(0x800000),
    HEVCMainTierLevel62(0x1000000),
    HEVCHighTierLevel62(0x2000000);

    companion object {
        fun from(findValue: Int): String? = try {
            HEVCLevels.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }

}