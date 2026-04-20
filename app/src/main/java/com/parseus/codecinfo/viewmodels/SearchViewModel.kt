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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchResultState(
    val audioResults: List<CodecSimpleInfo> = emptyList(),
    val videoResults: List<CodecSimpleInfo> = emptyList(),
    val drmResults: List<DrmSimpleInfo> = emptyList(),
    val isQueryEmpty: Boolean = true
)

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultState = MutableStateFlow(SearchResultState())
    val searchResultState: StateFlow<SearchResultState> = _searchResultState.asStateFlow()

    private var allAudio: List<CodecSimpleInfo> = emptyList()
    private var allVideo: List<CodecSimpleInfo> = emptyList()
    private var allDrms: List<DrmSimpleInfo> = emptyList()

    fun initData(context: Context) {
        if (allAudio.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            allAudio = getSimpleCodecInfoList(context, true)
            allVideo = getSimpleCodecInfoList(context, false)
            allDrms = getSimpleDrmInfoList(context)

            _searchQuery
                .map { query ->
                    if (query.isBlank()) {
                        SearchResultState(isQueryEmpty = true)
                    } else {
                        val queryWords = query.trim().split(Regex("\\s+"))
                        val filteredAudio = allAudio.filter { it.matches(queryWords) }
                        val filteredVideo = allVideo.filter { it.matches(queryWords) }
                        val filteredDrms = allDrms.filter { it.matches(queryWords) }
                        SearchResultState(filteredAudio, filteredVideo, filteredDrms, false)
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect { state ->
                    _searchResultState.value = state
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
