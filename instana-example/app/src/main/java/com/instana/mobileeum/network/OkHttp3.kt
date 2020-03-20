/*
* Created by Mikel Pascual (mikel@4rtstudio.com) on 12/03/2020.
*/
package com.instana.mobileeum.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object OkHttp3 {

    private val contentType = "application/json; charset=utf-8".toMediaType()
    private val okHttpClient = OkHttpClient.Builder().build()

    /**
     * Can't be run on Main Thread
     */
    fun executeRequest(useConstructor: Boolean, cancel: Boolean, method: String, url: String, body: String?): String {
        val requestBody =
            body?.toRequestBody(contentType)
        val request = Request.Builder().apply {
            addHeader("Accept", "application/json")
            addHeader("Accept-Encoding", "gzip,deflate")
            url(url)
            method(method, requestBody)
        }.build()

        val client = if (useConstructor) OkHttpClient() else okHttpClient
        val call = client.newCall(request)

        return try {
            if (cancel) {
                call.cancel()
                "cancelled"
            } else {
                call.execute().toString()
            }
        } catch (e: Exception) {
            e.toString()
        }
    }
}
