package com.mikhaellopez.ratebottomsheet

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.TooltipCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.RateBottomSheetLayoutBinding
import com.parseus.codecinfo.utils.getColorOnSurface
import com.parseus.codecinfo.utils.getOnPrimaryColor
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable

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
        val dialogThemeContext = ContextThemeWrapper(requireContext(), R.style.Theme_CodecInfo)
        val layoutInflater = LayoutInflater.from(dialogThemeContext)
        _binding = RateBottomSheetLayoutBinding.inflate(layoutInflater, container, false)
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

        if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
            view.applyMonetRecursively()
        }

        binding.apply {
            TooltipCompat.setTooltipText(btnRateBottomSheetCancel,
                getString(R.string.rate_popup_close_description))
            btnRateBottomSheetCancel.visibility =
                    if (RateBottomSheetManager.showCloseButtonIcon) View.VISIBLE else View.GONE

            btnRateBottomSheetCancel.setOnClickListener { dismiss() }
            btnRateBottomSheetNo.setOnClickListener { defaultBtnNoClickAction(it) }
            btnRateBottomSheetLater.setOnClickListener { defaultBtnLaterClickAction(it) }
        }

        updateColors()
    }

    private fun updateColors() {
        binding.run {
            val colorOnSurface = getColorOnSurface(requireContext())
            val colorPrimary = getPrimaryColor(requireContext())

            btnRateBottomSheetCancel.imageTintList = ColorStateList.valueOf(colorOnSurface)
            textRateBottomSheetTitle.setTextColor(colorOnSurface)
            textRateBottomSheetMessage.setTextColor(colorOnSurface)

            btnRateBottomSheetNo.setTextColor(colorPrimary)
            btnRateBottomSheetLater.setTextColor(colorPrimary)
            btnRateBottomSheetOk.setBackgroundColor(colorPrimary)
            btnRateBottomSheetOk.setTextColor(getOnPrimaryColor(requireContext()))
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

}