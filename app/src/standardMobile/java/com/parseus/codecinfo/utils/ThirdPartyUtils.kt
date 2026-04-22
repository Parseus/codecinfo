@file:Suppress("UNUSED_PARAMETER")

package com.parseus.codecinfo.utils

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.Library
import com.parseus.codecinfo.data.LicenseType
import com.parseus.codecinfo.ui.LicenseDialogManager

const val SHOW_RATE_APP = false

fun createInAppUpdateResultLauncher(activity: AppCompatActivity) {}
fun initializeAppRating(activity: AppCompatActivity) {}
fun launchStoreIntent(activity: android.app.Activity) {}
fun checkForUpdate(activity: android.app.Activity, progressBar: LinearProgressIndicator?) {}
fun handleAppUpdateOnActivityResult(activity: android.app.Activity, requestCode: Int, resultCode: Int) {}
fun handleAppUpdateOnResume(activity: android.app.Activity) {}

fun showLicensesDialog(activity: AppCompatActivity) {
    LicenseDialogManager(activity)
        .setLibrary(Library("AndroidHiddenApiBypass", "https://github.com/LSPosed/AndroidHiddenApiBypass", LicenseType.APACHE2))
        .setLibrary(Library("Android Jetpack", "https://developer.android.com/jetpack", LicenseType.APACHE2))
        .setLibrary(Library("Kotlin", "https://github.com/JetBrains/kotlin", LicenseType.APACHE2))
        .setLibrary(Library("Kotlin Coroutines", "https://github.com/Kotlin/kotlinx.coroutines", LicenseType.APACHE2))
        .setLibrary(Library("Kotlin Serialization", "https://github.com/Kotlin/kotlinx.serialization", LicenseType.APACHE2))
        .setLibrary(Library("LeakCanary", "https://github.com/square/leakcanary", LicenseType.APACHE2))
        .setLibrary(Library("Material Components for Android", "https://github.com/material-components/material-components-android", LicenseType.APACHE2))
        .setLibrary(Library("Okio", "https://github.com/square/okio", LicenseType.APACHE2))
        .setLibrary(Library("MonetCompat", "https://github.com/KieronQuinn/MonetCompat", LicenseType.MIT))
        .show()
}
