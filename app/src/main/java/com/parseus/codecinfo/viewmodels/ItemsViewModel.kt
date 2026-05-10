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
import com.parseus.codecinfo.utils.isTv
import com.parseus.codecinfo.utils.isWear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
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

    private val _isAmbientMode = MutableStateFlow(false)
    val isAmbientMode: StateFlow<Boolean> = _isAmbientMode.asStateFlow()

    private var preCacheJob: Job? = null

    private fun getPreCacheDelay(context: Context) =
        if (context.isTv() || context.isWear()) PRECACHE_DELAY_TV else PRECACHE_DELAY

    fun loadData(context: Context) {
        if (_allAudioState.value != null) return
        val appContext = context.applicationContext
        viewModelScope.launch {
            val audioJob = launch(Dispatchers.IO) {
                _allAudioState.value = getSimpleCodecInfoList(appContext, true)
            }
            val videoJob = launch(Dispatchers.IO) {
                _allVideoState.value = getSimpleCodecInfoList(appContext, false)
            }
            val drmJob = launch(Dispatchers.IO) {
                _allDrmsState.value = getSimpleDrmInfoList(appContext)
            }

            audioJob.join()
            videoJob.join()
            drmJob.join()

            preCacheDetails(appContext)
        }
    }

    fun refreshData(context: Context) {
        val appContext = context.applicationContext
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
                _allAudioState.value = getSimpleCodecInfoList(appContext, true)
            }
            val videoJob = launch(Dispatchers.IO) {
                _allVideoState.value = getSimpleCodecInfoList(appContext, false)
            }
            val drmJob = launch(Dispatchers.IO) {
                _allDrmsState.value = getSimpleDrmInfoList(appContext)
            }

            audioJob.join()
            videoJob.join()
            drmJob.join()

            preCacheDetails(appContext)
        }
    }

    private fun preCacheDetails(context: Context) {
        preCacheJob?.cancel()
        val appContext = context.applicationContext
        preCacheJob = viewModelScope.launch(Dispatchers.IO) {
            val audio = _allAudioState.value ?: emptyList()
            val video = _allVideoState.value ?: emptyList()
            val drms = _allDrmsState.value ?: emptyList()

            val highPriorityMimeTypes = listOf("avc", "hevc", "av01", "vp9", "aac", "mp3")

            val prioritizedCodecs = (audio + video).sortedWith(
                compareByDescending<CodecSimpleInfo> { it.isHardwareAccelereated }
                .thenByDescending { info ->
                    highPriorityMimeTypes.any { info.codecId.contains(it, ignoreCase = true) }
                }
            )

            val prioritizedDrms = drms.sortedByDescending {
                it.drmName.contains("Widevine", true) || it.drmName.contains("Clearkey", true)
            }

            for (info in prioritizedCodecs) {
                if (!isActive || _isAmbientMode.value) break
                getDetailedCodecInfo(appContext, info.codecId, info.codecName)
                yield()
                delay(getPreCacheDelay(appContext))
            }
            for (info in prioritizedDrms) {
                if (!isActive || _isAmbientMode.value) break
                getDetailedDrmInfo(appContext, info.drmUuid, DrmVendor.getFromUuid(info.drmUuid))
                yield()
                delay(getPreCacheDelay(appContext))
            }
        }
    }

    fun updateAudioList(context: Context) {
        if (_allAudioState.value != null) return
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            _allAudioState.value = getSimpleCodecInfoList(appContext, true)
        }
    }

    fun updateVideoList(context: Context) {
        if (_allVideoState.value != null) return
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            _allVideoState.value = getSimpleCodecInfoList(appContext, false)
        }
    }

    fun updateDrmList(context: Context) {
        if (_allDrmsState.value != null) return
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            _allDrmsState.value = getSimpleDrmInfoList(appContext)
        }
    }

    fun setAmbientMode(isAmbient: Boolean) {
        _isAmbientMode.value = isAmbient
    }

    companion object {
        // Precaching all details could potentially cause CPU spikes on lower-end devices,
        // so a small delay like that should (at least partially) avoid them.
        private const val PRECACHE_DELAY = 50L
        // Typically TV and wear devices feature an underpowered hardware, so I'm not confident
        // that mobile's 50 ms delay would be enough here.
        private const val PRECACHE_DELAY_TV = 100L
    }

}