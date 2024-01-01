@file:Suppress("unused")

package com.parseus.codecinfo.utils

import android.net.Uri

enum class InstallSource(val installerPackageName: String,
                         private val marketPrefix: String,
                         private val webPrefix: String) {

    AmazonAppstore("com.amazon.venezia",
            "amzn://apps/android?p=",
            "https://www.amazon.com/gp/mas/dl/android?p="),

    GalaxyStore("com.sec.android.app.samsungapps",
            "samsungapps://AppRating/",
            "https://www.samsungapps.com/appquery/AppRating.as?appId="),

    PlayStore("com.android.vending",
            "market://details?id=",
            "https://play.google.com/store/apps/details?id=");

    fun getMarketUri(packageName: String): Uri = Uri.parse("$marketPrefix$packageName")

    fun getWebUri(packageName: String): Uri = Uri.parse("$webPrefix$packageName")

    companion object {
        fun fromInstallSource(installSource: String?): InstallSource? {
            return entries.find { it.installerPackageName == installSource }
        }
    }

}