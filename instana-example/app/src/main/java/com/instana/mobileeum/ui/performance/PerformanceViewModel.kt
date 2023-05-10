/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.performance

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.lang.RuntimeException

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

    fun forceUnhandledException() {
        _response.postValue("Force Unhandled Exception")
        forceUnhandledExceptionWrapper("direct")
    }

    fun forceUnhandledExceptionInCoroutineBlocking() {
        runBlocking {
            coroutineScope {
                delay(1000)
                forceUnhandledExceptionInCoroutineImpl("runBlocking")
            }
        }
    }

    fun forceUnhandledExceptionInGlobalScope() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                delay(1000)
                forceUnhandledExceptionInCoroutineImpl("GlobalScope")
            }
        }
    }

    fun forceUnhandledExceptionInMainLooper() {
        Handler(Looper.getMainLooper()).post {
            forceUnhandledExceptionInCoroutineImpl("MainLooper")
        }
    }

    fun forceUnhandledExceptionInCoroutineImpl(mark: String) {
        _response.postValue("Force $mark Unhandled Exception")
        forceUnhandledExceptionWrapper(mark)
    }

    fun forceUnhandledExceptionWrapper(mark: String) {
        try {
            forceUnhandledExceptionInner(mark)
        } catch (ex: Exception) {
            throw RuntimeException("Inner Exception found - $mark", ex)
        }
    }

    fun forceUnhandledExceptionInner(mark: String) {
        throw RuntimeException("force $mark unhandled exception")
    }
}
