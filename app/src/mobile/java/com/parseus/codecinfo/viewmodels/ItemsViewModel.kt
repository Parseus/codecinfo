package com.parseus.codecinfo.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.clearCodecCaches
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.clearDrmCaches
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ItemsViewModel : ViewModel() {

    private val _allAudioState = MutableStateFlow<List<CodecSimpleInfo>?>(null)
    val allAudioState: StateFlow<List<CodecSimpleInfo>?> = _allAudioState.asStateFlow()

    private val _allVideoState = MutableStateFlow<List<CodecSimpleInfo>?>(null)
    val allVideoState: StateFlow<List<CodecSimpleInfo>?> = _allVideoState.asStateFlow()

    private val _allDrmsState = MutableStateFlow<List<DrmSimpleInfo>?>(null)
    val allDrmsState: StateFlow<List<DrmSimpleInfo>?> = _allDrmsState.asStateFlow()

    private val _refreshDetailsTrigger = MutableSharedFlow<Unit>()
    val refreshDetailsTrigger: SharedFlow<Unit> = _refreshDetailsTrigger.asSharedFlow()

    private var preCacheJob: Job? = null

    fun loadData(context: Context) {
        if (_allAudioState.value != null) return
        viewModelScope.launch {
            val audioJob = launch(Dispatchers.IO) {
                _allAudioState.value = getSimpleCodecInfoList(context, true)
            }
            val videoJob = launch(Dispatchers.IO) {
                _allVideoState.value = getSimpleCodecInfoList(context, false)
            }
            val drmJob = launch(Dispatchers.IO) {
                _allDrmsState.value = getSimpleDrmInfoList(context)
            }

            audioJob.join()
            videoJob.join()
            drmJob.join()

            preCacheDetails(context)
        }
    }

    fun refreshData(context: Context) {
        viewModelScope.launch {
            preCacheJob?.cancel()
            clearCodecCaches()
            clearDrmCaches()

            _allAudioState.value = null
            _allVideoState.value = null
            _allDrmsState.value = null

            _refreshDetailsTrigger.emit(Unit)

            // Force immediate reload instead of waiting for next loadData call
            val audioJob = launch(Dispatchers.IO) {
                _allAudioState.value = getSimpleCodecInfoList(context, true)
            }
            val videoJob = launch(Dispatchers.IO) {
                _allVideoState.value = getSimpleCodecInfoList(context, false)
            }
            val drmJob = launch(Dispatchers.IO) {
                _allDrmsState.value = getSimpleDrmInfoList(context)
            }

            audioJob.join()
            videoJob.join()
            drmJob.join()

            preCacheDetails(context)
        }
    }

    private fun preCacheDetails(context: Context) {
        preCacheJob?.cancel()
        preCacheJob = viewModelScope.launch(Dispatchers.IO) {
            val audio = _allAudioState.value ?: emptyList()
            val video = _allVideoState.value ?: emptyList()
            val drms = _allDrmsState.value ?: emptyList()

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

    fun updateAudioList(context: Context) {
        if (_allAudioState.value != null) return
        viewModelScope.launch(Dispatchers.IO) {
            _allAudioState.value = getSimpleCodecInfoList(context, true)
        }
    }

    fun updateVideoList(context: Context) {
        if (_allVideoState.value != null) return
        viewModelScope.launch(Dispatchers.IO) {
            _allVideoState.value = getSimpleCodecInfoList(context, false)
        }
    }

    fun updateDrmList(context: Context) {
        if (_allDrmsState.value != null) return
        viewModelScope.launch(Dispatchers.IO) {
            _allDrmsState.value = getSimpleDrmInfoList(context)
        }
    }

    companion object {
        // Precaching all details could potentially cause CPU spikes on lower-end devices,
        // so a small delay like that should (at least partially) avoid them.
        private const val PRECACHE_DELAY = 50L
    }

}