package com.parseus.codecinfo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import java.lang.ref.WeakReference
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * RateThisApp<br>
 * A library to show the app rate dialog
 * @author Keisuke Kobayashi (k.kobayashi.122@gmail.com)
 *
 */
object RateThisApp {

    private val TAG = RateThisApp::class.java.simpleName

    private const val PREF_NAME = "RateThisApp"
    private const val KEY_INSTALL_DATE = "rta_install_date"
    private const val KEY_LAUNCH_TIMES = "rta_launch_times"
    private const val KEY_OPT_OUT = "rta_opt_out"
    private const val KEY_ASK_LATER_DATE = "rta_ask_later_date"

    @JvmStatic
    private var installDate = Date()
    @JvmStatic
    private var launchTimes = 0
    @JvmStatic
    private var optOut = false
    @JvmStatic
    private var askLaterDate = Date()

    @JvmStatic
    private var config: Config = Config()
    @Suppress("MemberVisibilityCanBePrivate")
    @JvmStatic
    var callback: Callback? = null

    @JvmStatic
    private var dialogRef: WeakReference<AlertDialog>? = null

    /**
     * Initialize RateThisApp configuration.
     * @param config Configuration object.
     */
    fun init(config: Config) {
        this.config = config
    }

    /**
     * Call this API when the launcher activity is launched.<br>
     * It is better to call this API in onCreate() of the launcher activity.
     * @param context Context
     */
    fun onCreate(context: Context) {
        Runnable {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            // If it is the first launch, save the date in shared preference.
            if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
                storeInstallDate(context, editor)
            }
            // Increment launch times
            var launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0)
            launchTimes++
            editor.putInt(KEY_LAUNCH_TIMES, launchTimes)

            editor.apply()

            installDate = Date(pref.getLong(KEY_INSTALL_DATE, 0))
            this.launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0)
            optOut = pref.getBoolean(KEY_OPT_OUT, false)
            askLaterDate = Date(pref.getLong(KEY_ASK_LATER_DATE, 0))

            printStatus(context)
        }.run()
    }

    /**
     * Show the rate dialog if the criteria is satisfied.
     * @param context Context
     * @param themeId Theme ID
     * @return true if shown, false otherwise.
     */
    fun showRateDialogIfNeeded(context: Context, themeId: Int = 0): Boolean {
        return if (shouldShowRateDialog()) {
            showRateDialog(context, themeId)
            true
        } else {
            false
        }
    }

    /**
     * Check whether the rate dialog should be shown or not.
     * Developers may call this method directly if they want to show their own view instead of
     * dialog provided by this library.
     * @return
     */
    private fun shouldShowRateDialog(): Boolean {
        if (optOut) {
            return false
        } else {
            if (launchTimes >= config.criteriaLaunchTimes) {
                return true
            }
            val threshold = TimeUnit.DAYS.toMillis(config.criteriaInstallDays.toLong())   // msec
            return Date().time - installDate.time >= threshold && Date().time - askLaterDate.time >= threshold
        }
    }

    /**
     * Show the rate dialog
     * @param context
     * @param themeId
     */
    private fun showRateDialog(context: Context, @StyleRes themeId: Int = 0) {
        if (dialogRef?.get() != null) {
            // Dialog is already present
            return
        }

        val titleId = if (config.titleId != 0) config.titleId else R.string.rta_dialog_title
        val messageId = if (config.messageId != 0) config.messageId else R.string.rta_dialog_message
        val cancelButtonID = if (config.cancelButton != 0) config.cancelButton else R.string.rta_dialog_cancel
        val thanksButtonID = if (config.noButtonId != 0) config.noButtonId else R.string.rta_dialog_no
        val rateButtonID = if (config.yesButtonId != 0) config.yesButtonId else R.string.rta_dialog_ok
        val builder = AlertDialog.Builder(context, themeId).apply {
            setTitle(titleId)
            setMessage(messageId)
            when (config.cancelMode) {
                Config.CANCEL_MODE_BACK_KEY_OR_TOUCH_OUTSIDE -> setCancelable(true)
                Config.CANCEL_MODE_BACK_KEY -> {
                    setCancelable(false)
                    setOnKeyListener { dialog, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.cancel()
                            true
                        } else {
                            false
                        }
                    }
                }
                Config.CANCEL_MODE_NONE -> setCancelable(false)
            }
            setPositiveButton(rateButtonID) { _, _ ->
                callback?.onYesClicked()
                val appPackage = context.packageName
                var url = "market://details?id=$appPackage"
                if (!TextUtils.isEmpty(config.url)) {
                    url = config.url!!
                }
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (anfe: android.content.ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + appPackage)))
                }

                setOptOut(context, true)
            }
            setNeutralButton(cancelButtonID) { _, _ ->
                callback?.onCancelClicked()
                clearSharedPreferences(context)
                storeAskLaterDate(context)
            }
            setNegativeButton(thanksButtonID) { _, _ ->
                callback?.onNoClicked()
                setOptOut(context, true)
            }
            setOnCancelListener {
                callback?.onCancelClicked()
                clearSharedPreferences(context)
                storeAskLaterDate(context)
            }
            setOnDismissListener { dialogRef?.clear() }
        }
        dialogRef = WeakReference(builder.show())
    }

    /**
     * Clear data in shared preferences.<br></br>
     * This API is called when the "Later" is pressed or canceled.
     * @param context
     */
    private fun clearSharedPreferences(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply { 
            remove(KEY_INSTALL_DATE)
            remove(KEY_LAUNCH_TIMES)
            apply()
        }
        launchTimes = 0
        installDate.time = 0
    }

    /**
     * Set opt out flag.
     * If it is true, the rate dialog will never shown unless app data is cleared.
     * This method is called when Yes or No is pressed.
     * @param context
     * @param optOut
     */
    private fun setOptOut(context: Context, optOut: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply { 
            putBoolean(KEY_OPT_OUT, optOut)
            apply()
        }
        this.optOut = optOut
    }

    /**
     * Store install date.
     * Install date is retrieved from package manager if possible.
     * @param context
     * @param editor
     */
    private fun storeInstallDate(context: Context, editor: SharedPreferences.Editor) {
        var installDate = Date()
        val packMan = context.packageManager
        try {
            val pkgInfo = packMan.getPackageInfo(context.packageName, 0)
            installDate = Date(pkgInfo.firstInstallTime)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        editor.putLong(KEY_INSTALL_DATE, installDate.time)
        log("First install: " + installDate.toString())
    }

    /**
     * Store the date the user asked for being asked again later.
     * @param context
     */
    private fun storeAskLaterDate(context: Context) {
        val currentTime = System.currentTimeMillis()
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply { 
            putLong(KEY_ASK_LATER_DATE, currentTime)
            apply()
        }
        askLaterDate.time = currentTime
    }

    /**
     * Print values in SharedPreferences (used for debug)
     * @param context
     */
    private fun printStatus(context: Context) {
        with(context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)) {
            log("*** RateThisApp Status ***")
            log("Install Date: " + Date(getLong(KEY_INSTALL_DATE, 0)))
            log("Launch Times: " + getInt(KEY_LAUNCH_TIMES, 0))
            log("Opt out: " + getBoolean(KEY_OPT_OUT, false))
        }
    }

    /**
     * Print log if enabled
     * @param message
     */
    private fun log(message: String) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Log.v(TAG, message)
        }
    }

    /**
     * RateThisApp configuration.
     */
    data class Config(val criteriaInstallDays: Int = 7, val criteriaLaunchTimes: Int = 10) {
        var url: String? = null
        @StringRes var titleId = 0
        @StringRes var messageId = 0
        @StringRes var yesButtonId = 0
        @StringRes var noButtonId = 0
        @StringRes var cancelButton = 0
        var cancelMode = CANCEL_MODE_BACK_KEY_OR_TOUCH_OUTSIDE

        companion object {
            const val CANCEL_MODE_BACK_KEY_OR_TOUCH_OUTSIDE = 0
            const val CANCEL_MODE_BACK_KEY                  = 1
            const val CANCEL_MODE_NONE                      = 2
        }
    }

    /**
     * Callback of dialog click event
     */
    interface Callback {
        /**
         * "Rate now" event
         */
        fun onYesClicked()

        /**
         * "No, thanks" event
         */
        fun onNoClicked()

        /**
         * "Later" event
         */
        fun onCancelClicked()
    }

}