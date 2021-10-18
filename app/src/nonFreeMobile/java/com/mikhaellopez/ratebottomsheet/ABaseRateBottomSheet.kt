package com.mikhaellopez.ratebottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import com.parseus.codecinfo.databinding.RateBottomSheetLayoutBinding
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

        if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
            view.applyMonetRecursively()
        }

        binding.apply {
            btnRateBottomSheetCancel.visibility =
                    if (RateBottomSheetManager.showCloseButtonIcon) View.VISIBLE else View.GONE

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

}