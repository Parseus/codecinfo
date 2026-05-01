package com.parseus.codecinfo.ui.externalLinks

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExternalLinksViewModel : ViewModel() {
    val launchExternalLink = MutableLiveData<Uri?>()
    val prefetchExternalLink = MutableLiveData<Uri?>()
    var urlOpened: Uri? = null
}