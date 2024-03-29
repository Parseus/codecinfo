package com.mikhaellopez.ratebottomsheet

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.utils.InstallSource

/**
 * Copyright (C) 2020 Mikhael LOPEZ
 * Licensed under the Apache License Version 2.0
 */
@Suppress("ProtectedInFinal")
class RateBottomSheet : ABaseRateBottomSheet() {

    protected var installSource: InstallSource? = null
    protected var actionListener: ActionListener? = null

    /**
     * You can use this listener if you choose to setShowAskBottomSheet(false)
     * Otherwise, consider using .AskRateBottomSheet.ActionListener
     *
     * Each callback has an empty body, meaning that is optional
     */
    interface ActionListener {
        /**
         * Will be called when a click on the "Rate" button is triggered
         */
        fun onRateClickListener() {}

        /**
         * Will be called when a click on the "No thanks" button is triggered
         */
        fun onNoClickListener() {}
    }

    companion object {
        internal fun showDialog(manager: FragmentManager, source: InstallSource?, listener: ActionListener? = null) {
            RateBottomSheet().apply {
                installSource = source
                actionListener = listener
                show(manager, "rateBottomSheet")
            }
        }

        /**
         * Display rate bottom sheet if meets conditions.
         *
         * @param activity [AppCompatActivity]
         * @param listener [AskRateBottomSheet.ActionListener]
         */
        fun showRateBottomSheetIfMeetsConditions(
            activity: AppCompatActivity,
            installSource: InstallSource?,
            listener: AskRateBottomSheet.ActionListener? = null
        ) {
            showRateBottomSheetIfMeetsConditions(
                activity.applicationContext,
                activity.supportFragmentManager,
                installSource,
                listener
            )
        }

        /**
         * Display rate bottom sheet if meets conditions.
         *
         * @param fragment [Fragment]
         * @param listener [AskRateBottomSheet.ActionListener]
         */
        fun showRateBottomSheetIfMeetsConditions(
            fragment: Fragment,
            installSource: InstallSource,
            listener: AskRateBottomSheet.ActionListener? = null
        ) {
            (fragment.activity as? AppCompatActivity)?.also {
                showRateBottomSheetIfMeetsConditions(
                    it.applicationContext,
                    fragment.childFragmentManager,
                    installSource,
                    listener
                )
            }
        }

        /**
         * Display rate bottom sheet if meets conditions.
         *
         * @param context [Context]
         * @param fragmentManager [FragmentManager]
         * @param listener [AskRateBottomSheet.ActionListener]
         */
        fun showRateBottomSheetIfMeetsConditions(
            context: Context,
            fragmentManager: FragmentManager,
            installSource: InstallSource?,
            listener: AskRateBottomSheet.ActionListener? = null
        ) {
            if (RateBottomSheetManager(context).shouldShowRateBottomSheet()) {
                if (RateBottomSheetManager.showAskBottomSheet) {
                    AskRateBottomSheet.showDialog(fragmentManager, installSource, listener)
                } else {
                    showDialog(fragmentManager, installSource)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnRateBottomSheetLater.visibility =
                    if (RateBottomSheetManager.showLaterButton) View.VISIBLE else View.GONE

            textRateBottomSheetTitle.text = getString(R.string.rate_popup_title)
            textRateBottomSheetMessage.text = getString(R.string.rate_popup_message)
            btnRateBottomSheetNo.text = getString(R.string.rate_popup_no)
            btnRateBottomSheetLater.text = getString(R.string.rate_popup_later)
            btnRateBottomSheetOk.text = getString(R.string.rate_popup_ok)

            btnRateBottomSheetOk.setOnClickListener {
                activity?.run {
                    val source = if (!RateBottomSheetManager.debugForceOpenEnable) {
                        installSource
                    } else {
                        InstallSource.PlayStore
                    }
                    if (source != null) {
                        openStore(packageName, source)
                    }
                    RateBottomSheetManager(it.context).disableAgreeShowDialog()
                }
                dismiss()
                actionListener?.onRateClickListener()
            }

            btnRateBottomSheetNo.setOnClickListener {
                defaultBtnNoClickAction(it)
                actionListener?.onNoClickListener()
            }
        }
    }

    private fun Activity.openStore(appPackageName: String, installSource: InstallSource) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, installSource.getMarketUri(appPackageName)))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, installSource.getWebUri(appPackageName)))
        }
    }

}