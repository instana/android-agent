/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.okhttp3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instana.mobileeum.network.OkHttp3
import com.instana.mobileeum.network.RequestPreset
import com.instana.mobileeum.network.RequestPresetFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OkHttp3ViewModel : ViewModel() {

    private val _presets = MutableLiveData<List<RequestPreset>>().apply {
        value = RequestPresetFactory.generalPresets
    }
    val presets: LiveData<List<RequestPreset>> = _presets

    private val _response = MutableLiveData<String>().apply {
        value = ""
    }
    val response: LiveData<String> = _response

    fun executeRequest(useConstructor: Boolean, cancel: Boolean, method: String, url: String, body: String?) {
        _response.postValue("")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _response.postValue(OkHttp3.executeRequest(useConstructor, cancel, method, url, body))
            }
        }
    }
}
