/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.performance

import android.app.Application
import android.content.ComponentCallbacks2
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PerformanceViewModel : ViewModel() {
    private val _response = MutableLiveData<String>().apply {
        value = ""
    }
    val response: LiveData<String> = _response

    fun forceLowMemory(application: Application) {
        _response.postValue("")
        application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        _response.postValue("Forced LowMemory event")
    }

    fun forceFrameSkip() {
        _response.postValue("")
        Thread.sleep(300)
        _response.postValue("Forced FrameSkip event")
    }

    fun forceANR() {
        _response.postValue("")
        Thread.sleep(12000)
        _response.postValue("Forced ANR event")
    }
}
