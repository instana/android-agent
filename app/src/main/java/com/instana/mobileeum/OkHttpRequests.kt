package com.instana.mobileeum

import android.util.Log
import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.instrumentation.RemoteCallMarker
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object OkHttpRequests {

    private const val TAG = "OkHttpRequests"

    private val contentType = MediaType.get("application/json; charset=utf-8")
    private val okHttpClient = OkHttpClient.Builder().build()

    fun executeGet(url: String = "https://reqres.in/api/users/23", enableManual: Boolean): Boolean {
        var tracker: RemoteCallMarker? = null
        if (enableManual) {
            tracker = Instana.remoteCallInstrumentation?.markCall(url, "GET")
        }
        val requestBuilder = Request.Builder()
                .url(url)
                .get()

        tracker?.let {
            requestBuilder.header(tracker.headerKey(), tracker.headerValue())
        }

        val request = requestBuilder.build()

        return try {
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            tracker?.endedWith(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.endedWith(e)
            false
        } finally {
            request.header(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
        }
    }

    fun executePost(
            url: String = "https://reqres.in/api/users",
            json: String = """{"name": "morpheus","job": "leader"}""",
            enableManual: Boolean
    ): Boolean {
        val body = RequestBody.create(contentType, json)
        var tracker: RemoteCallMarker? = null
        if (enableManual) {
            tracker = Instana.remoteCallInstrumentation?.markCall(url, "POST")
        }
        val requestBuilder = Request.Builder()
                .url(url)
                .post(body)

        tracker?.let {
            requestBuilder.header(tracker.headerKey(), tracker.headerValue())
        }

        val request = requestBuilder.build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            tracker?.endedWith(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.endedWith(e)
            false
        } finally {
            request.header(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
        }
    }

    fun executeDelete(url: String = "https://reqres.in/api/users/2", enableManual: Boolean): Boolean {
        var tracker: RemoteCallMarker? = null
        if (enableManual) {
            tracker = Instana.remoteCallInstrumentation?.markCall(url, "DELETE")
        }
        val requestBuilder = Request.Builder()
                .url(url)
                .delete()
        tracker?.let {
            requestBuilder.header(tracker.headerKey(), tracker.headerValue())
        }

        val request = requestBuilder.build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            tracker?.endedWith(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.endedWith(e)
            false
        } finally {
            request.header(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
        }
    }

    fun executePut(
            url: String = "https://reqres.in/api/users/2",
            json: String = """{"name": "morpheus","job": "zion resident"}""",
            enableManual: Boolean
    ): Boolean {
        val body = RequestBody.create(contentType, json)
        var tracker: RemoteCallMarker? = null
        if (enableManual) {
            tracker = Instana.remoteCallInstrumentation?.markCall(url, "PUT")
        }
        val requestBuilder = Request.Builder()
                .url(url)
                .put(body)

        tracker?.let {
            requestBuilder.header(tracker.headerKey(), tracker.headerValue())
        }

        val request = requestBuilder.build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            tracker?.endedWith(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tracker?.endedWith(e)
            false
        } finally {
            request.header(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
        }
    }
}