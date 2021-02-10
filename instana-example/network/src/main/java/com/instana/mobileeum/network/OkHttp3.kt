/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.network

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

object OkHttp3 {

    @Suppress("DEPRECATION_ERROR")
    private val contentType = MediaType.parse("application/json; charset=utf-8")
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor()).build()

    /**
     * Can't be run on Main Thread
     */
    fun executeRequest(useConstructor: Boolean, cancel: Boolean, method: String, url: String, body: String?): String {
        return try {
            val requestBody = body?.let { RequestBody.create(contentType, it) }
            val request = Request.Builder().apply {
                addHeader("Accept", "application/json")
                addHeader("Accept-Encoding", "gzip,deflate")
                url(url)
                method(method, requestBody)
            }.build()

            val client = if (useConstructor) OkHttpClient() else okHttpClient
            val call = client.newCall(request)
            if (cancel) {
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {}
                })
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
