/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.transitives

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instana.mobileeum.network.Retrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransitivesViewModel : ViewModel() {
    private val _response = MutableLiveData<String>().apply {
        value = ""
    }
    val response: LiveData<String> = _response

    fun executeRetrofitRequest() {
        _response.postValue("")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Retrofit().executeRequest { response ->
                    _response.postValue(response)
                }
            }
        }
    }
}
