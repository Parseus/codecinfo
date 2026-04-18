package com.parseus.codecinfo.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("unused")
class TvSearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider,
        OnItemViewClickedListener {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private var searchJob: Job? = null

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
        searchJob?.cancel()

        if (query.isEmpty()) {
            rowsAdapter.clear()
            return
        }

        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(SEARCH_DELAY_MS)

            val rows = withContext(Dispatchers.Default) {
                buildSearchResultRows(query)
            }

            rowsAdapter.clear()
            rowsAdapter.addAll(0, rows)
        }
    }

    private fun buildSearchResultRows(query: String): List<ListRow> {
        val context = requireContext()
        val results = mutableListOf<ListRow>()

        val audioCodecs = getSimpleCodecInfoList(context, true).filter {
            it.codecId.contains(query, true) || it.codecName.contains(query, true)
        }
        createListRow(1, R.string.category_audio, R.drawable.ic_audio, CodecPresenter(R.drawable.ic_audio), audioCodecs)?.let {
            results.add(it)
        }

        val videoCodecs = getSimpleCodecInfoList(context, false).filter {
            it.codecId.contains(query, true) || it.codecName.contains(query, true)
        }
        createListRow(2, R.string.category_video, R.drawable.ic_video, CodecPresenter(R.drawable.ic_video), videoCodecs)?.let {
            results.add(it)
        }

        val drmInfo = getSimpleDrmInfoList(context).filter {
            it.drmName.contains(query, true)
        }
        createListRow(3, R.string.category_drm, R.drawable.ic_lock, DrmPresenter(R.drawable.ic_lock), drmInfo)?.let {
            results.add(it)
        }

        return results
    }

    private fun <T> createListRow(
        id: Long,
        @StringRes titleRes: Int,
        @DrawableRes iconRes: Int,
        presenter: Presenter,
        items: List<T>
    ): ListRow? {
        if (items.isEmpty()) return null

        val header = HeaderItem(id, getString(titleRes))
        val adapter = ArrayObjectAdapter(presenter).apply {
            addAll(0, items)
        }
        return ListRow(header, adapter)
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

    companion object {
        private const val SEARCH_DELAY_MS = 300L
    }

}