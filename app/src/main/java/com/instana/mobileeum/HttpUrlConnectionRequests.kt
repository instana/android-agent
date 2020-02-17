package com.instana.mobileeum

import android.util.Log
import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.instrumentation.RemoteCallMarker
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection

object HttpUrlConnectionRequests {

    private const val TAG = "HttpRequests"
    private const val ERROR = "error"

    fun doGet(
        desiredUrl: String = "https://httpstat.us/200",
        enableManual: Boolean
    ): Boolean {
        var urlConnection: HttpsURLConnection? = null
        var marker: RemoteCallMarker? = null
        try {
            urlConnection = URL(desiredUrl).openConnection() as HttpsURLConnection
            if (enableManual) {
                marker = Instana.remoteCallInstrumentation!!.markCall(desiredUrl, "GET")
                urlConnection.setRequestProperty(marker.headerKey(), marker.headerValue())
            }
            urlConnection.requestMethod = "GET"
            urlConnection.setRequestProperty("Accept", "application/json")
            urlConnection.doOutput = true
            urlConnection.connect()
            val responseCode = urlConnection.responseCode
            marker?.endedWith(urlConnection)
            return responseCode == HttpsURLConnection.HTTP_OK
        } catch (e: IOException) {
            Log.e(TAG, ERROR, e)
            marker?.endedWith(urlConnection!!, e)
            return false
        } finally {
            urlConnection?.getRequestProperty(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
            urlConnection?.disconnect()
        }
    }

    fun doPost(
        desiredUrl: String = "https://reqres.in/api/users",
        json: String = """{"name": "morpheus","job": "zion resident"}""",
        enableManual: Boolean
    ): Boolean {
        var urlConnection: HttpURLConnection? = null
        var marker: RemoteCallMarker? = null
        try {
            urlConnection = URL(desiredUrl).openConnection() as HttpURLConnection
            if (enableManual) {
                marker = Instana.remoteCallInstrumentation!!.markCall(desiredUrl, "POST")
                urlConnection.setRequestProperty(marker.headerKey(), marker.headerValue())
            }
            urlConnection.doOutput = true
            urlConnection.requestMethod = "POST"
            urlConnection.connect()

            //Write
            val outputStream = urlConnection.outputStream
            val writer = OutputStreamWriter(outputStream, Charset.defaultCharset())
            writer.write(json)
            writer.close()
            outputStream.close()

            val responseCode = urlConnection.responseCode
            marker?.endedWith(urlConnection)
            return responseCode == HttpsURLConnection.HTTP_OK
        } catch (e: IOException) {
            Log.e(TAG, ERROR, e)
            marker?.endedWith(urlConnection!!, e)
            return false
        } finally {
            urlConnection?.getRequestProperty(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
            urlConnection?.disconnect()
        }
    }

    fun doDelete(
        desiredUrl: String = "https://reqres.in/api/users/2",
        enableManual: Boolean
    ): Boolean {
        var urlConnection: HttpURLConnection? = null
        var marker: RemoteCallMarker? = null
        try {
            urlConnection = URL(desiredUrl).openConnection() as HttpURLConnection
            if (enableManual) {
                marker = Instana.remoteCallInstrumentation!!.markCall(desiredUrl, "DELETE")
                urlConnection.setRequestProperty(marker.headerKey(), marker.headerValue())
            }
            urlConnection.doOutput = true
            urlConnection.requestMethod = "DELETE"
            urlConnection.connect()

            val responseCode = urlConnection.responseCode
            marker?.endedWith(urlConnection)
            return responseCode == HttpsURLConnection.HTTP_OK
        } catch (e: IOException) {
            Log.e(TAG, ERROR, e)
            marker?.endedWith(urlConnection!!, e)
            return false
        } finally {
            urlConnection?.getRequestProperty(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
            urlConnection?.disconnect()
        }
    }

    fun doPut(
        desiredUrl: String = "https://reqres.in/api/users/2",
        json: String = """{"name": "morpheus","job": "zion resident"}""",
        enableManual: Boolean
    ): Boolean {
        var urlConnection: HttpURLConnection? = null
        var marker: RemoteCallMarker? = null
        try {
            urlConnection = URL(desiredUrl).openConnection() as HttpURLConnection
            if (enableManual) {
                marker = Instana.remoteCallInstrumentation!!.markCall(desiredUrl, "PUT")
                urlConnection.setRequestProperty(marker.headerKey(), marker.headerValue())
            }
            urlConnection.doOutput = true
            urlConnection.requestMethod = "PUT"
            urlConnection.connect()

            //Write
            val outputStream = urlConnection.outputStream
            val writer = OutputStreamWriter(outputStream, Charset.defaultCharset())
            writer.write(json)
            writer.close()
            outputStream.close()

            val responseCode = urlConnection.responseCode
            marker?.endedWith(urlConnection)
            return responseCode == HttpsURLConnection.HTTP_OK
        } catch (e: IOException) {
            Log.e(TAG, ERROR, e)
            marker?.endedWith(urlConnection!!, e)
            return false
        } finally {
            urlConnection?.getRequestProperty(TRACKING_HEADER_KEY)?.run {
                Log.e(TAG, this)
            }
            urlConnection?.disconnect()
        }
    }
}