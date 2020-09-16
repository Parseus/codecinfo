package com.mikhaellopez.ratebottomsheet

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.parseus.codecinfo.databinding.RateBottomSheetLayoutBinding

/**
 * Copyright (C) 2020 Mikhael LOPEZ
 * Licensed under the Apache License Version 2.0
 */
abstract class ABaseRateBottomSheet : BottomSheetDialogFragment() {

    private var _binding: RateBottomSheetLayoutBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RateBottomSheetLayoutBinding.inflate(inflater, container, false)
        dialog?.setOnShowListener { dialog ->
            (dialog as? BottomSheetDialog)?.also {
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.also { bottomSheetInternal ->
                        BottomSheetBehavior.from(bottomSheetInternal).state =
                            BottomSheetBehavior.STATE_EXPANDED
                    }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnRateBottomSheetCancel.visibility =
                    if (RateBottomSheetManager.showCloseButtonIcon) View.VISIBLE else View.GONE

            context?.also {
                btnRateBottomSheetOk.backgroundTintList =
                        ColorStateList.valueOf(getThemeAccentColor(it))
            }

            btnRateBottomSheetCancel.setOnClickListener { dismiss() }
            btnRateBottomSheetNo.setOnClickListener { defaultBtnNoClickAction(it) }
            btnRateBottomSheetLater.setOnClickListener { defaultBtnLaterClickAction(it) }
        }
    }

    protected fun defaultBtnNoClickAction(view: View) {
        RateBottomSheetManager(view.context).disableAgreeShowDialog()
        dismiss()
    }

    private fun defaultBtnLaterClickAction(view: View) {
        RateBottomSheetManager(view.context).setRemindInterval()
        dismiss()
    }

    private fun getThemeAccentColor(context: Context): Int {
        val colorAttr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.R.attr.colorAccent
        } else { // Get colorAccent defined for AppCompat
            context.resources.getIdentifier("colorAccent", "attr", context.packageName)
        }
        val outValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, outValue, true)
        return outValue.data
    }

}