package com.instana.mobileeum.ui.custom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instana.android.CustomEvent
import com.instana.android.Instana
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomEventViewModel : ViewModel() {

    private val _presets = MutableLiveData<List<RequestPreset>>().apply {
        value = RequestPresetFactory.generalPresets
    }
    val presets: LiveData<List<RequestPreset>> = _presets

    fun sendCustomEvent(
        eventName: String,
        startTimeEpochMs: Long?,
        durationMs: Long?,
        viewName: String?,
        meta: Map<String, String>?,
        backendTracingID: String?,
        errorMessage: String?
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val error = errorMessage?.let { Throwable(errorMessage) }
                val event = CustomEvent(eventName).apply {
                    this.startTime = startTimeEpochMs
                    this.duration = durationMs
                    this.viewName = viewName
                    this.meta = meta
                    this.backendTracingID = backendTracingID
                    this.error = error
                }
                Instana.reportEvent(event)
            }
        }
    }
}