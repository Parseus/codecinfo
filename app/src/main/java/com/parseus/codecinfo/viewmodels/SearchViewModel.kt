package com.parseus.codecinfo.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import com.parseus.codecinfo.utils.matches
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchResultState(
    val audioResults: List<CodecSimpleInfo> = emptyList(),
    val videoResults: List<CodecSimpleInfo> = emptyList(),
    val drmResults: List<DrmSimpleInfo> = emptyList(),
    val isQueryEmpty: Boolean = true,
    val query: String = ""
)

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val _searchResultState = MutableStateFlow(SearchResultState())
    val searchResultState: StateFlow<SearchResultState> = _searchResultState.asStateFlow()

    private var allAudio: List<CodecSimpleInfo> = emptyList()
    private var allVideo: List<CodecSimpleInfo> = emptyList()
    private var allDrms: List<DrmSimpleInfo> = emptyList()

    private var initJob: Job? = null

    fun initData(context: Context) {
        if (allAudio.isNotEmpty() || initJob?.isActive == true) return
        val appContext = context.applicationContext

        initJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val audioDeferred = async { getSimpleCodecInfoList(appContext, true) }
                val videoDeferred = async { getSimpleCodecInfoList(appContext, false) }
                val drmDeferred = async { getSimpleDrmInfoList(appContext) }

                allAudio = audioDeferred.await()
                allVideo = videoDeferred.await()
                allDrms = drmDeferred.await()

                searchQuery
                    .debounce(100L)
                    .map { query ->
                        if (query.isBlank()) {
                            SearchResultState(isQueryEmpty = true, query = query)
                        } else {
                            val queryWords = query.trim().split(Regex("\\s+"))
                            val filteredAudio = allAudio.filter { it.matches(queryWords) }
                            val filteredVideo = allVideo.filter { it.matches(queryWords) }
                            val filteredDrms = allDrms.filter { it.matches(queryWords) }
                            SearchResultState(filteredAudio, filteredVideo, filteredDrms, false, query)
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .collect { state ->
                        _searchResultState.value = state
                    }
            } catch (_: Exception) {}
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }
}
