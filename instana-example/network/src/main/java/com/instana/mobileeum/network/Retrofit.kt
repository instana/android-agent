/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.network

import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


data class Change(
    val subject: String? = null
)

interface GerritAPI {
    @GET("changes/")
    fun loadChanges(@Query("q") status: String?): Call<List<Change>>
}

class Retrofit : Callback<List<Change>> {

    var onResponse: (String) -> Unit = {}

    fun executeRequest(onResponseListener: (String) -> Unit) {
        onResponse = onResponseListener

        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://git.eclipse.org/r/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val gerritAPI = retrofit.create(GerritAPI::class.java)
        val call = gerritAPI.loadChanges("status:open")
        call.enqueue(this)
    }

    override fun onResponse(
        call: Call<List<Change>>,
        response: Response<List<Change>>
    ) {
        if (response.isSuccessful) {
            onResponse("Fetched ${response.body()?.size} changes")
        } else {
            onResponse(response.errorBody()?.string() ?: response.code().toString())
        }
    }

    override fun onFailure(
        call: Call<List<Change>>,
        t: Throwable
    ) {
        onResponse(t.toString())
    }
}
