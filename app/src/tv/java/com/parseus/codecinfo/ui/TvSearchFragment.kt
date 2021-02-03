package com.parseus.codecinfo.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList

@Suppress("unused")
class TvSearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider,
        OnItemViewClickedListener {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private val handler = Handler(Looper.getMainLooper())
    private val delayedLoad = SearchRunnable()

    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)
        setOnItemViewClickedListener(this)
    }

    override fun onQueryTextChange(newQuery: String): Boolean {
        handleSearch(newQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        handleSearch(query)
        return true
    }

    private fun handleSearch(query: String) {
        rowsAdapter.clear()
        if (query.isNotEmpty()) {
            delayedLoad.searchQuery = query
            handler.removeCallbacks(delayedLoad)
            handler.postDelayed(delayedLoad, SEARCH_DELAY_MS)
        }
    }

    fun loadRows(query: String) {
        val audioCodecList = getSimpleCodecInfoList(requireContext(), true)
        val filteredAudioCodecList = filterCodecs(audioCodecList, query)
        if (filteredAudioCodecList.isNotEmpty()) {
            val audioPresenterHeader = HeaderItem(1, getString(R.string.category_audio))
            val audioPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_audio))

            for (audioCodec in filteredAudioCodecList) {
                audioPresentAdapter.add(audioCodec)
            }

            rowsAdapter.add(ListRow(audioPresenterHeader, audioPresentAdapter))
        }

        val videoCodecList = getSimpleCodecInfoList(requireContext(), false)
        val filteredVideoCodecList = filterCodecs(videoCodecList, query)
        if (filteredVideoCodecList.isNotEmpty()) {
            val videoPresenterHeader = HeaderItem(2, getString(R.string.category_video))
            val videoPresentAdapter = ArrayObjectAdapter(CodecPresenter(R.drawable.ic_video))

            for (videoCodec in filteredVideoCodecList) {
                videoPresentAdapter.add(videoCodec)
            }

            rowsAdapter.add(ListRow(videoPresenterHeader, videoPresentAdapter))
        }

        val drmInfoList = getSimpleDrmInfoList(requireContext())
        val filteredDrmList = filterDrm(drmInfoList, query)
        if (filteredDrmList.isNotEmpty()) {
            val drmPresenterHeader = HeaderItem(3, getString(R.string.category_drm))
            val drmPresentAdapter = ArrayObjectAdapter(DrmPresenter(R.drawable.ic_lock))

            for (drmInfo in filteredDrmList) {
                drmPresentAdapter.add(drmInfo)
            }

            rowsAdapter.add(ListRow(drmPresenterHeader, drmPresentAdapter))
        }
    }

    private fun filterCodecs(infoList: List<CodecSimpleInfo>, query: String): List<CodecSimpleInfo> {
        return infoList.filter {
            it.codecId.contains(query, true) || it.codecName.contains(query, true)
        }
    }

    private fun filterDrm(infoList: List<DrmSimpleInfo>, query: String): List<DrmSimpleInfo> {
        return infoList.filter {
            it.drmName.contains(query, true)
        }
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
        }
    }

    private inner class SearchRunnable : Runnable {

        lateinit var searchQuery: String

        override fun run() {
            loadRows(searchQuery)
        }
    }

    companion object {
        private const val SEARCH_DELAY_MS = 300L
    }

}