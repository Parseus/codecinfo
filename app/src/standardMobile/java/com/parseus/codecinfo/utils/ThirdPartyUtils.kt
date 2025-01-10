@file:Suppress("UNUSED_PARAMETER")

package com.parseus.codecinfo.utils

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.parseus.codecinfo.R
import com.parseus.codecinfo.ui.LicenserMaterialDialog

const val SHOW_RATE_APP = false

fun createInAppUpdateResultLauncher(activity: AppCompatActivity) {}
fun initializeAppRating(activity: AppCompatActivity) {}
fun launchStoreIntent(activity: android.app.Activity) {}
fun checkForUpdate(activity: android.app.Activity, progressBar: LinearProgressIndicator?) {}
fun handleAppUpdateOnActivityResult(activity: android.app.Activity, requestCode: Int, resultCode: Int) {}
fun handleAppUpdateOnResume(activity: android.app.Activity) {}

fun showLicensesDialog(activity: AppCompatActivity) {
    LicenserMaterialDialog(activity)
        .setTitle(R.string.about_licenses)
        .setLibrary(Library("AndroidHiddenApiBypass", "https://github.com/LSPosed/AndroidHiddenApiBypass", License.APACHE2))
        .setLibrary(Library("Android Jetpack", "https://developer.android.com/jetpack", License.APACHE2))
        .setLibrary(Library("Kotlin", "https://github.com/JetBrains/kotlin", License.APACHE2))
        .setLibrary(Library("Kotlin Coroutines", "https://github.com/Kotlin/kotlinx.coroutines", License.APACHE2))
        .setLibrary(Library("LeakCanary", "https://github.com/square/leakcanary", License.APACHE2))
        .setLibrary(Library("Material Components for Android", "https://github.com/material-components/material-components-android", License.APACHE2))
        .setLibrary(Library("Moshi", "https://github.com/square/moshi", License.APACHE2))
        .setLibrary(Library("Okio", "https://github.com/square/okio", License.APACHE2))
        .setLibrary(Library("Licenser", "https://github.com/marcoscgdev/Licenser", License.MIT))
        .setLibrary(Library("MonetCompat", "https://github.com/KieronQuinn/MonetCompat", License.MIT))
        .setPositiveButton(android.R.string.ok)
        .setBackgroundColor(getSurfaceContainerHighColor(activity))
        .show()
}