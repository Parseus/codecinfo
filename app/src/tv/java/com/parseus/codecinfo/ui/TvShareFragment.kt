package com.parseus.codecinfo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.parseus.codecinfo.R
import com.parseus.codecinfo.utils.getAllInfoString
import com.parseus.codecinfo.utils.getCodecAndDrmItemListString

class TvShareFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val title = getString(R.string.action_share)
        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_share)
        return GuidanceStylist.Guidance(title, null, null, icon)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        val shareItemListAction = GuidedAction.Builder(requireContext())
            .title(R.string.codec_drm_list).id(ACTION_SHARE_ITEM_LIST).build()
        val shareAllInfoAction = GuidedAction.Builder(requireContext())
            .title(R.string.codec_drm_all_info).id(ACTION_SHARE_ALL_INFO).build()
        val cancelAction = GuidedAction.Builder(requireContext()).title(android.R.string.cancel)
            .clickAction(GuidedAction.ACTION_ID_CANCEL).build()
        actions.add(shareItemListAction)
        actions.add(shareAllInfoAction)
        actions.add(cancelAction)
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (action.id == ACTION_SHARE_ITEM_LIST || action.id == ACTION_SHARE_ALL_INFO) {
            launchShareIntent(action.id)
        }
        finishGuidedStepSupportFragments()
    }

    private fun launchShareIntent(actionId: Long) {
        val textToShare = when (actionId) {
            ACTION_SHARE_ITEM_LIST -> getCodecAndDrmItemListString(requireContext())
            ACTION_SHARE_ALL_INFO -> getAllInfoString(requireContext())
            else -> ""
        }
        val shareIntent = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)
            val title = getString(if (actionId == ACTION_SHARE_ITEM_LIST)
                R.string.codec_drm_list else R.string.codec_drm_all_info)
            putExtra(Intent.EXTRA_TITLE, title)
        }, null)
        startActivity(shareIntent)
    }

    companion object {
        private const val ACTION_SHARE_ITEM_LIST = -1000L
        private const val ACTION_SHARE_ALL_INFO = -1001L
    }

}