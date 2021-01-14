package com.parseus.codecinfo.tv

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.parseus.codecinfo.R
import com.parseus.codecinfo.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.drm.DrmSimpleInfo
import com.parseus.codecinfo.drm.getSimpleDrmInfoList

@Suppress("unused")
class MainTvFragment : BrowseSupportFragment(), OnItemViewClickedListener {

    private lateinit var adapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        brandColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.teal_700)
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        adapter = ArrayObjectAdapter(ListRowPresenter())
        setAdapter(adapter)

        val audioCodecList = getSimpleCodecInfoList(requireContext(), true)
        val audioPresenterHeader = HeaderItem(1, getString(R.string.category_audio))
        val audioPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_audio))

        for (audioCodec in audioCodecList) {
            audioPresentAdapter.add(audioCodec)
        }

        adapter.add(ListRow(audioPresenterHeader, audioPresentAdapter))

        val videoCodecList = getSimpleCodecInfoList(requireContext(), false)
        val videoPresenterHeader = HeaderItem(2, getString(R.string.category_video))
        val videoPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_video))

        for (videoCodec in videoCodecList) {
            videoPresentAdapter.add(videoCodec)
        }

        adapter.add(ListRow(videoPresenterHeader, videoPresentAdapter))

        val drmInfoList = getSimpleDrmInfoList()
        val drmPresenterHeader = HeaderItem(3, getString(R.string.category_drm))
        val drmPresentAdapter = ArrayObjectAdapter(DrmPresenter(R.drawable.ic_lock))

        for (drmInfo in drmInfoList) {
            drmPresentAdapter.add(drmInfo)
        }

        adapter.add(ListRow(drmPresenterHeader, drmPresentAdapter))

        val otherPresenterHeader = HeaderItem(4, getString(R.string.category_other))
        val otherPresenterAdapter = ArrayObjectAdapter(OtherActionsPresenter())

        otherPresenterAdapter.add(OtherActionDescriptor(ACTION_SETTINGS_ID, R.drawable.ic_settings, R.string.settings))
        otherPresenterAdapter.add(OtherActionDescriptor(ACTION_ABOUT_ID, R.drawable.ic_info, R.string.about_app))

        adapter.add(ListRow(otherPresenterHeader, otherPresenterAdapter))

        onItemViewClickedListener = this
    }

    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                               rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {
        if (item is CodecSimpleInfo) {
            val intent = Intent(requireActivity(), TvCodecDetailsActivity::class.java).apply {
                putExtra("codecId", item.codecId)
                putExtra("codecName", item.codecName)
            }
            startActivity(intent)
        } else if (item is DrmSimpleInfo) {
            val intent = Intent(requireActivity(), TvCodecDetailsActivity::class.java).apply {
                putExtra("drmName", item.drmName)
                putExtra("drmUuid", item.drmUuid)
            }
            startActivity(intent)
        } else if (item is OtherActionDescriptor) {
            if (item.actionId == ACTION_SETTINGS_ID) {
                startActivity(Intent(requireActivity(), TvSettingsActivity::class.java))
            } else if (item.actionId == ACTION_ABOUT_ID) {
                startActivity(Intent(requireActivity(), TvAboutActivity::class.java))
            }
        }
    }

    companion object {
        private const val ACTION_SETTINGS_ID = 1000
        private const val ACTION_ABOUT_ID = 1001
    }

}