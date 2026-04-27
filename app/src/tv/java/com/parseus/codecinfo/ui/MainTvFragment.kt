package com.parseus.codecinfo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.audioCodecList
import com.parseus.codecinfo.data.codecinfo.detailedCodecInfos
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.codecinfo.videoCodecList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.detailedDrmInfo
import com.parseus.codecinfo.data.drm.drmList
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.ui.settings.SettingsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("unused")
class MainTvFragment : BrowseSupportFragment(), OnItemViewClickedListener {

    private val audioPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_audio))
    private val videoPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_video))
    private val drmPresentAdapter = ArrayObjectAdapter(DrmPresenter(R.drawable.ic_lock))

    private val settingsContract = registerForActivityResult(SettingsContract()) { result ->
        if (result.shouldReloadLists() || result.saveDetailsToLogcatChanged) {
            viewLifecycleOwner.lifecycleScope.launch {
                clearSavedLists()
                loadData()
            }
        }
    }

    private lateinit var adapter: ArrayObjectAdapter

    private var preCacheJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().fullyDrawnReporter.addReporter()

        setupUI()
        setupAdapter()

        viewLifecycleOwner.lifecycleScope.launch {
            loadData()
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

        adapter.add(ListRow(otherPresenterHeader, otherPresenterAdapter))
    }

    private suspend fun loadData() = coroutineScope {
        val context = context ?: return@coroutineScope

        val audioJob = launch(Dispatchers.IO) {
            val audioList = getSimpleCodecInfoList(context, true)
            withContext(Dispatchers.Main) {
                audioPresentAdapter.setItems(audioList, null)
            }
        }
        val videoJob = launch(Dispatchers.IO) {
            val videoList = getSimpleCodecInfoList(context, false)
            withContext(Dispatchers.Main) {
                videoPresentAdapter.setItems(videoList, null)
            }
        }
        val drmJob = launch(Dispatchers.IO) {
            val drmsList = getSimpleDrmInfoList(context)
            withContext(Dispatchers.Main) {
                drmPresentAdapter.setItems(drmsList, null)
            }
        }

        audioJob.join()
        videoJob.join()
        drmJob.join()

        requireActivity().fullyDrawnReporter.removeReporter()

        preCacheDetails(
            audioPresentAdapter.unmodifiableList(),
            videoPresentAdapter.unmodifiableList(),
            drmPresentAdapter.unmodifiableList()
        )
    }

    private fun preCacheDetails(audio: List<CodecSimpleInfo>, video: List<CodecSimpleInfo>, drms: List<DrmSimpleInfo>) {
        val context = context ?: return
        preCacheJob?.cancel()
        preCacheJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            for (info in audio) {
                getDetailedCodecInfo(context, info.codecId, info.codecName)
                delay(PRECACHE_DELAY)
            }
            for (info in video) {
                getDetailedCodecInfo(context, info.codecId, info.codecName)
                delay(PRECACHE_DELAY)
            }
            for (info in drms) {
                getDetailedDrmInfo(context, info.drmUuid, DrmVendor.getFromUuid(info.drmUuid))
                delay(PRECACHE_DELAY)
            }
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

    companion object {
        // Typically TV devices feature an underpowered hardware, so I'm not confident
        // that mobile's 50 ms delay would be enough here.
        private const val PRECACHE_DELAY = 100L

        private const val ACTION_SETTINGS_ID = 1000
        private const val ACTION_ABOUT_ID = 1001
        private const val ACTION_DEVICE_ISSUES_ID = 1002
        private const val ACTION_SHARE_ID = 1003
        private const val ACTION_KEYBOARD_SHORTCUTS_ID = 1004
    }

}