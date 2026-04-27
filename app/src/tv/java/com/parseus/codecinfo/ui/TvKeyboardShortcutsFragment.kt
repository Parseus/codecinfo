package com.parseus.codecinfo.ui

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.parseus.codecinfo.R

class TvKeyboardShortcutsFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return GuidanceStylist.Guidance(
            getString(R.string.keyboard_shortcuts),
            null,
            null,
            null
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val context = requireContext()
        actions.add(GuidedAction.Builder(context)
            .title(getString(R.string.keyboard_key_ctrl_f))
            .description(getString(R.string.keyboard_shortcut_search))
            .focusable(false)
            .build())
        actions.add(GuidedAction.Builder(context)
            .title(getString(R.string.keyboard_key_ctrl_s))
            .description(getString(R.string.keyboard_shortcut_share))
            .focusable(false)
            .build())
        actions.add(GuidedAction.Builder(context)
            .title(getString(R.string.keyboard_key_ctrl_comma))
            .description(getString(R.string.keyboard_shortcut_settings))
            .focusable(false)
            .build())
        actions.add(GuidedAction.Builder(context)
            .title(getString(R.string.keyboard_key_ctrl_c))
            .description(getString(R.string.keyboard_shortcut_copy))
            .focusable(false)
            .build())
        actions.add(GuidedAction.Builder(context)
            .title(getString(R.string.keyboard_key_ctrl_v))
            .description(getString(R.string.keyboard_shortcut_paste))
            .focusable(false)
            .build())
        
        actions.add(GuidedAction.Builder(context)
            .id(GuidedAction.ACTION_ID_OK)
            .title(android.R.string.ok)
            .build())
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (action.id == GuidedAction.ACTION_ID_OK) {
            requireActivity().finish()
        }
    }

}