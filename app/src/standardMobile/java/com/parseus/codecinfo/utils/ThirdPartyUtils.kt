@file:Suppress("UNUSED_PARAMETER")

package com.parseus.codecinfo.utils

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.VARIANT_LIBRARIES
import com.parseus.codecinfo.ui.LicenseDialogManager

const val SHOW_RATE_APP = false

fun createInAppUpdateResultLauncher(activity: AppCompatActivity) {}
fun initializeAppRating(activity: AppCompatActivity) {}
fun launchStoreIntent(activity: android.app.Activity) {}
fun checkForUpdate(activity: android.app.Activity, progressBar: LinearProgressIndicator?) {}
fun handleAppUpdateOnActivityResult(activity: android.app.Activity, requestCode: Int, resultCode: Int) {}
fun handleAppUpdateOnResume(activity: android.app.Activity) {}

fun showLicensesDialog(activity: AppCompatActivity) {
    val manager = LicenseDialogManager(activity)
    VARIANT_LIBRARIES.forEach { manager.setLibrary(it) }
    manager.show()
}
