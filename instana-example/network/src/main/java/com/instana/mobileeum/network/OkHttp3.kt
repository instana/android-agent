/*
* Created by Mikel Pascual (mikel@4rtstudio.com) on 12/03/2020.
*/
package com.instana.mobileeum.network

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor

object OkHttp3 {

    @Suppress("DEPRECATION_ERROR")
    private val contentType = MediaType.parse("application/json; charset=utf-8")
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor()).build()

    /**
     * Can't be run on Main Thread
     */
    fun executeRequest(useConstructor: Boolean, cancel: Boolean, method: String, url: String, body: String?): String {
        val requestBody = body?.let { RequestBody.create(contentType, it) }
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
