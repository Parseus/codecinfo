package com.parseus.codecinfo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.tracing.trace
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.audioCodecList
import com.parseus.codecinfo.data.codecinfo.detailedCodecInfos
import com.parseus.codecinfo.data.codecinfo.videoCodecList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.detailedDrmInfo
import com.parseus.codecinfo.data.drm.drmList
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.ui.settings.SettingsContract
import com.parseus.codecinfo.viewmodels.ItemsViewModel
import kotlinx.coroutines.launch

@Suppress("unused")
class MainTvFragment : BrowseSupportFragment(), OnItemViewClickedListener {

    private val audioPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_audio))
    private val videoPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_video))
    private val drmPresentAdapter = ArrayObjectAdapter(DrmPresenter(R.drawable.ic_lock))

    private val viewModel: ItemsViewModel by activityViewModels()

    private val settingsContract = registerForActivityResult(SettingsContract()) { result ->
        if (result.shouldReloadLists() || result.saveDetailsToLogcatChanged) {
            clearSavedLists()
            viewModel.refreshData(requireContext())
        }
    }

    private lateinit var adapter: ArrayObjectAdapter

    private var isFullyDrawnReporterAdded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = trace("TvMainFragment.onViewCreated") {
        super.onViewCreated(view, savedInstanceState)

        addFullyDrawnReporter()

        setupUI()
        setupAdapter()

        viewModel.loadData(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allAudioState.collect {
                        it?.let {
                            audioPresentAdapter.setItems(it, object : DiffCallback<CodecSimpleInfo>() {
                                override fun areItemsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo): Boolean {
                                    return oldItem.codecId == newItem.codecId && oldItem.codecName == newItem.codecName
                                }
                                override fun areContentsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo): Boolean {
                                    return oldItem == newItem
                                }
                            })
                            checkIfFullyDrawn()
                        }
                    }
                }
                launch {
                    viewModel.allVideoState.collect {
                        it?.let {
                            videoPresentAdapter.setItems(it, object : DiffCallback<CodecSimpleInfo>() {
                                override fun areItemsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo): Boolean {
                                    return oldItem.codecId == newItem.codecId && oldItem.codecName == newItem.codecName
                                }
                                override fun areContentsTheSame(oldItem: CodecSimpleInfo, newItem: CodecSimpleInfo): Boolean {
                                    return oldItem == newItem
                                }
                            })
                            checkIfFullyDrawn()
                        }
                    }
                }
                launch {
                    viewModel.allDrmsState.collect {
                        it?.let {
                            drmPresentAdapter.setItems(it, object : DiffCallback<DrmSimpleInfo>() {
                                override fun areItemsTheSame(oldItem: DrmSimpleInfo, newItem: DrmSimpleInfo): Boolean {
                                    return oldItem.drmUuid == newItem.drmUuid && oldItem.drmName == newItem.drmName
                                }
                                override fun areContentsTheSame(oldItem: DrmSimpleInfo, newItem: DrmSimpleInfo): Boolean {
                                    return oldItem == newItem
                                }
                            })
                            checkIfFullyDrawn()
                        }
                    }
                }
            }
        }
    }

    private fun checkIfFullyDrawn() {
        if (viewModel.allAudioState.value != null && viewModel.allVideoState.value != null
            && viewModel.allDrmsState.value != null) {
            removeFullyDrawnReporter()
        }
    }

    private fun setupUI() {
        brandColor = requireContext().getColor(R.color.purple_600)
        searchAffordanceColor = requireContext().getColor(R.color.teal_700)
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        onItemViewClickedListener = this
        setOnSearchClickedListener { startActivity(Intent(requireActivity(), TvSearchActivity::class.java)) }
    }

    private fun setupAdapter() {
        adapter = ArrayObjectAdapter(ListRowPresenter()).apply {
            add(ListRow(HeaderItem(1, getString(R.string.category_audio)), audioPresentAdapter))
            add(ListRow(HeaderItem(2, getString(R.string.category_video)), videoPresentAdapter))
            add(ListRow(HeaderItem(3, getString(R.string.category_drm)), drmPresentAdapter))
        }
        setAdapter(adapter)
        setupOtherActionsRow()
    }

    private fun setupOtherActionsRow() {
        val otherPresenterHeader = HeaderItem(4, getString(R.string.category_other))
        val otherPresenterAdapter = ArrayObjectAdapter(OtherActionsPresenter())

        val affectedByKnownProblems = DEVICE_PROBLEMS_DB.any {
            it.isAffected(requireContext(), null)
        }
        if (affectedByKnownProblems) {
            otherPresenterAdapter.add(
                OtherActionDescriptor(
                    ACTION_DEVICE_ISSUES_ID,
                    R.drawable.ic_warning,
                    R.string.known_issue_warning
                )
            )
        }
        otherPresenterAdapter.add(
            OtherActionDescriptor(
                ACTION_SHARE_ID,
                R.drawable.ic_share,
                R.string.action_share
            )
        )
        otherPresenterAdapter.add(
            OtherActionDescriptor(
                ACTION_KEYBOARD_SHORTCUTS_ID,
                R.drawable.ic_keyboard,
                R.string.keyboard_shortcuts
            )
        )
        otherPresenterAdapter.add(
            OtherActionDescriptor(
                ACTION_SETTINGS_ID,
                R.drawable.ic_settings,
                R.string.action_settings
            )
        )
        otherPresenterAdapter.add(
            OtherActionDescriptor(
                ACTION_ABOUT_ID,
                R.drawable.ic_info,
                R.string.about_app
            )
        )

        val listRow = ListRow(otherPresenterHeader, otherPresenterAdapter)
        if (adapter.size() > 3) {
            adapter.replace(3, listRow)
        } else {
            adapter.add(listRow)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun clearSavedLists() {
        audioCodecList.clear()
        videoCodecList.clear()
        drmList.clear()
        detailedCodecInfos.clear()
        detailedDrmInfo.clear()
    }

    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                               rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {
        when (item) {
            is CodecSimpleInfo -> {
                val intent = Intent(requireActivity(), TvCodecDetailsActivity::class.java).apply {
                    putExtra("codecId", item.codecId)
                    putExtra("codecName", item.codecName)
                }
                startActivity(intent)
            }
            is DrmSimpleInfo -> {
                val intent = Intent(requireActivity(), TvCodecDetailsActivity::class.java).apply {
                    putExtra("drmName", item.drmName)
                    putExtra("drmUuid", item.drmUuid)
                }
                startActivity(intent)
            }
            is OtherActionDescriptor -> {
                when (item.actionId) {
                    ACTION_SETTINGS_ID -> settingsContract.launch(null)
                    ACTION_ABOUT_ID -> startActivity(Intent(requireActivity(), TvAboutActivity::class.java))
                    ACTION_DEVICE_ISSUES_ID -> startActivity(Intent(requireActivity(), TvDeviceIssuesActivity::class.java))
                    ACTION_SHARE_ID -> startActivity(Intent(requireActivity(), TvShareActivity::class.java))
                    ACTION_KEYBOARD_SHORTCUTS_ID -> startActivity(Intent(requireActivity(), TvKeyboardShortcutsActivity::class.java))
                }
            }
        }
    }

    private fun addFullyDrawnReporter() {
        if (!isFullyDrawnReporterAdded) {
            requireActivity().fullyDrawnReporter.addReporter()
            isFullyDrawnReporterAdded = true
        }
    }

    private fun removeFullyDrawnReporter() {
        if (isFullyDrawnReporterAdded) {
            activity?.fullyDrawnReporter?.removeReporter()
            isFullyDrawnReporterAdded = false
        }
    }

    override fun onDestroyView() {
        removeFullyDrawnReporter()
        super.onDestroyView()
    }

    companion object {
        private const val ACTION_SETTINGS_ID = 1000
        private const val ACTION_ABOUT_ID = 1001
        private const val ACTION_DEVICE_ISSUES_ID = 1002
        private const val ACTION_SHARE_ID = 1003
        private const val ACTION_KEYBOARD_SHORTCUTS_ID = 1004
    }

}