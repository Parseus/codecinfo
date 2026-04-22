package com.parseus.codecinfo.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.DiffCallback
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import com.parseus.codecinfo.utils.getHighlightedText
import com.parseus.codecinfo.utils.matches
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

            rowsAdapter.setItems(rows, object : DiffCallback<ListRow>() {
                override fun areItemsTheSame(oldItem: ListRow, newItem: ListRow): Boolean {
                    return oldItem.headerItem.id == newItem.headerItem.id
                }

                override fun areContentsTheSame(oldItem: ListRow, newItem: ListRow): Boolean {
                    return oldItem.headerItem.name == newItem.headerItem.name
                            && oldItem.adapter.size() == newItem.adapter.size()
                }
            })
        }
    }

    private suspend fun buildSearchResultRows(query: String): List<ListRow> = coroutineScope {
        val context = requireContext()
        val results = mutableListOf<ListRow>()
        val queryWords = query.trim().split(Regex("\\s+"))
        val primaryColor = context.getColor(R.color.teal_200)

        val audioDeferred = async(Dispatchers.IO) { getSimpleCodecInfoList(context, true) }
        val videoDeferred = async(Dispatchers.IO) { getSimpleCodecInfoList(context, false) }
        val drmDeferred = async(Dispatchers.IO) { getSimpleDrmInfoList(context) }

        val audioCodecs = audioDeferred.await().filter {
            it.matches(queryWords)
        }.map {
            CodecSearchItem(it, getHighlightedText(it.codecId, query, primaryColor),
                getHighlightedText(it.codecName, query, primaryColor))
        }
        createListRow(1, R.string.category_audio, R.drawable.ic_audio, CodecPresenter(R.drawable.ic_audio), audioCodecs)?.let {
            results.add(it)
        }

        val videoCodecs = videoDeferred.await().filter {
            it.matches(queryWords)
        }.map {
            CodecSearchItem(it, getHighlightedText(it.codecId, query, primaryColor),
                getHighlightedText(it.codecName, query, primaryColor))
        }
        createListRow(2, R.string.category_video, R.drawable.ic_video, CodecPresenter(R.drawable.ic_video), videoCodecs)?.let {
            results.add(it)
        }

        val drmInfo = drmDeferred.await().filter {
            it.matches(queryWords)
        }.map {
            DrmSearchItem(it, getHighlightedText(it.drmName, query, primaryColor))
        }
        createListRow(3, R.string.category_drm, R.drawable.ic_lock, DrmPresenter(R.drawable.ic_lock), drmInfo)?.let {
            results.add(it)
        }

        results
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
        if (item is CodecSearchItem) {
            val info = item.info
            val intent = Intent(requireActivity(), TvCodecDetailsActivity::class.java).apply {
                putExtra("codecId", info.codecId)
                putExtra("codecName", info.codecName)
            }
            startActivity(intent)
        } else if (item is DrmSearchItem) {
            val info = item.info
            val intent = Intent(requireActivity(), TvCodecDetailsActivity::class.java).apply {
                putExtra("drmName", info.drmName)
                putExtra("drmUuid", info.drmUuid)
            }
            startActivity(intent)
        }
    }

    companion object {
        private const val SEARCH_DELAY_MS = 200L
    }

}