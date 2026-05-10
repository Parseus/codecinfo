package com.parseus.codecinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.InfoType
import com.parseus.codecinfo.databinding.WearFragmentShareBinding
import com.parseus.codecinfo.ui.adapters.MoreAction
import com.parseus.codecinfo.ui.adapters.WearMoreAdapter
import com.parseus.codecinfo.utils.applyRotaryInput
import com.parseus.codecinfo.utils.getAllInfoString
import com.parseus.codecinfo.utils.getItemListString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WearShareFragment : Fragment() {

    private var _binding: WearFragmentShareBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearFragmentShareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                parentFragmentManager.popBackStack()
            }
        })

        val infoType = InfoType.currentInfoType
        val titleResId = if (infoType == InfoType.DRM) R.string.drm_list else R.string.codec_list

        val actions = listOf(
            MoreAction(ID_SHARE_ITEM_LIST, titleResId, R.drawable.ic_share),
            MoreAction(ID_SHARE_ALL_INFO, R.string.codec_drm_all_info, R.drawable.ic_share)
        )

        val adapter = WearMoreAdapter { action -> launchShareIntent(action.id) }

        binding.wearableRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(requireContext())
            this.adapter = adapter
            applyRotaryInput()
            requestFocus()
        }
        adapter.submitList(actions)
    }

    private fun launchShareIntent(actionId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val isDrm = InfoType.currentInfoType == InfoType.DRM
            val (textToShare, titleResId) = withContext(Dispatchers.IO) {
                when (actionId) {
                    ID_SHARE_ITEM_LIST -> getItemListString(
                        requireContext()) to
                            (if (isDrm) R.string.drm_list else R.string.codec_list)
                    ID_SHARE_ALL_INFO -> getAllInfoString(requireContext()) to R.string.codec_drm_all_info
                    else -> "" to 0
                }
            }
            if (titleResId == 0) return@launch

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textToShare)
                putExtra(Intent.EXTRA_TITLE, getString(titleResId))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.wearableRecyclerView.requestFocus()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ID_SHARE_ITEM_LIST = 1
        private const val ID_SHARE_ALL_INFO = 2
    }

}