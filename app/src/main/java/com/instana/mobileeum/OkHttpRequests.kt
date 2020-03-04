package com.instana.mobileeum

import com.instana.android.Instana
import com.instana.android.instrumentation.HTTPMarker
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object OkHttpRequests {

    private const val TAG = "OkHttpRequests"

    private val contentType = MediaType.get("application/json; charset=utf-8")
    private val okHttpClient = OkHttpClient.Builder().build()

    fun executeGetIgnored() = executeGet("https://www.google.com", false)
    fun executeGetSuccess() = executeGet("https://sesandbox-instana.instana.io", false)
    fun executeGetFailure() = executeGet("https://httpstat.us/404", false)
    fun executeGetException() = executeGet("https://httpstat-nonexistingurlhere.us/200", false)
    fun executeGetCancelled() = executeGetCancelled("https://httpstat.us/200", false)

    private fun executeGet(url: String = "https://httpstat.us/404", enableManual: Boolean): Boolean {
        var tracker: HTTPMarker? = null
        if (enableManual) {
            tracker = Instana.startCapture(url)
        }
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Encoding", "gzip,deflate")
            .get()
            .build()

        return try {
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            tracker?.finish(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.finish(request, e)
            false
        }
    }
    private fun executeGetCancelled(url: String = "https://httpstat.us/404", enableManual: Boolean): Boolean {
        var tracker: HTTPMarker? = null
        if (enableManual) {
            tracker = Instana.startCapture(url)
        }
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Encoding", "gzip,deflate")
            .get()
            .build()

        return try {
            val client = OkHttpClient()
            val response = client.newCall(request).cancel()
            tracker?.cancel()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.finish(request, e)
            false
        }
    }

    fun executePostSuccess() = executePost("https://reqres.in/api/users", """{"name": "morpheus","job": "leader"}""", false)
    fun executePostFailure() = executePost("https://httpstat.us/403", """{"name": "morpheus","job": "leader"}""", false)
    private fun executePost(
        url: String = "https://reqres.in/api/users",
        json: String = """{"name": "morpheus","job": "leader"}""",
        enableManual: Boolean
    ): Boolean {
        val body = RequestBody.create(contentType, json)
        var tracker: HTTPMarker? = null
        if (enableManual) {
            tracker = Instana.startCapture(url)
        }
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            tracker?.finish(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.finish(request, e)
            false
        }
    }

    fun executeDelete(url: String = "https://reqres.in/api/users/2", enableManual: Boolean): Boolean {
        var tracker: HTTPMarker? = null
        if (enableManual) {
            tracker = Instana.startCapture(url)
        }
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            tracker?.finish(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.finish(request, e)
            false
        }
    }

    fun executePut(
        url: String = "https://reqres.in/api/users/2",
        json: String = """{"name": "morpheus","job": "zion resident"}""",
        enableManual: Boolean
    ): Boolean {
        val body = RequestBody.create(contentType, json)
        var tracker: HTTPMarker? = null
        if (enableManual) {
            tracker = Instana.startCapture(url)
        }

        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            tracker?.finish(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.finish(request, e)
            false
        }
    }
}