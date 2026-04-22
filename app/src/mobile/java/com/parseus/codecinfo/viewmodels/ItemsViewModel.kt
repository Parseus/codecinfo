package com.parseus.codecinfo.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.codecinfo.getSimpleCodecInfoList
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import com.parseus.codecinfo.data.drm.getSimpleDrmInfoList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemsViewModel : ViewModel() {

    private val _allAudioState = MutableStateFlow<List<CodecSimpleInfo>?>(null)
    val allAudioState: StateFlow<List<CodecSimpleInfo>?> = _allAudioState.asStateFlow()

    private val _allVideoState = MutableStateFlow<List<CodecSimpleInfo>?>(null)
    val allVideoState: StateFlow<List<CodecSimpleInfo>?> = _allVideoState.asStateFlow()

    private val _allDrmsState = MutableStateFlow<List<DrmSimpleInfo>?>(null)
    val allDrmsState: StateFlow<List<DrmSimpleInfo>?> = _allDrmsState.asStateFlow()

    fun loadData(context: Context) {
        if (_allAudioState.value != null) return
        viewModelScope.launch(Dispatchers.IO) {
            val audioDeferred = async { getSimpleCodecInfoList(context, true) }
            val videoDeferred = async { getSimpleCodecInfoList(context, false) }
            val drmDeferred = async { getSimpleDrmInfoList(context) }

            _allAudioState.value = audioDeferred.await()
            _allVideoState.value = videoDeferred.await()
            _allDrmsState.value = drmDeferred.await()
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
}