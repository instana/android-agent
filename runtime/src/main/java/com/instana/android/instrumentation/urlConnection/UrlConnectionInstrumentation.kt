/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.instrumentation.urlConnection

import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.checkTag
import com.instana.android.core.util.ConstantsAndUtil.hasTrackingHeader
import com.instana.android.core.util.ConstantsAndUtil.isAutoEnabled
import com.instana.android.core.util.ConstantsAndUtil.isBlacklistedURL
import com.instana.android.core.util.ConstantsAndUtil.isLibraryCallBoolean
import com.instana.android.core.util.Logger
import com.instana.android.core.util.getRequestHeadersMap
import com.instana.android.core.util.instanaGenericExceptionFallbackHandler
import com.instana.android.instrumentation.HTTPMarker
import java.util.concurrent.ConcurrentHashMap


@Suppress("unused")
class UrlConnectionInstrumentation {

    companion object {

        private val httpMarkers = ConcurrentHashMap<String, HTTPMarker>()

        @JvmStatic
        fun openConnection(connection: java.net.URLConnection) {
            Logger.i("HttpURLConnection: intercepting openConnection")
            try {
                val header = connection.getRequestProperty(TRACKING_HEADER_KEY)
                val url = connection.url.toString()

                Instana.config?.takeIf { it.enableW3CHeaders }?.let {
                    ConstantsAndUtil.generateW3CHeaders().forEach { (key, value) -> connection.setRequestProperty(key, value) }
                }
                if (isAutoEnabled && !checkTag(header) && !isLibraryCallBoolean(url) && !isBlacklistedURL(url)) {
                    val marker = Instana.startCapture(url = url)
                    if (marker != null) {
                        connection.setRequestProperty(TRACKING_HEADER_KEY, marker.headerValue())
                        val requestHeaders = ConstantsAndUtil.getCapturedRequestHeaders(connection.getRequestHeadersMap())
                        marker.headers.putAll(requestHeaders)
                        httpMarkers[marker.headerValue()] = marker
                    }
                }
            }catch (e:Exception){
                e.instanaGenericExceptionFallbackHandler(classType = "UrlConnectionInstrumentation", at = "openConnection with java.net.URLConnection")
            }
        }

        @JvmStatic
        fun disconnect(connection: java.net.HttpURLConnection) {
            Logger.i("HttpURLConnection: intercepting disconnect")
            try {
                val header = connection.getRequestProperty(TRACKING_HEADER_KEY)
                val url = connection.url.toString()

                if (isAutoEnabled && !isLibraryCallBoolean(url) && checkTag(header)) {
                    httpMarkers[header]?.finish(connection)
                    httpMarkers.remove(header)
                }
            }catch (e:Exception){
                e.instanaGenericExceptionFallbackHandler(classType = "UrlConnectionInstrumentation", at = "disconnect with java.net.HttpURLConnection")
            }
        }

        @JvmStatic
        fun handleException(connection: java.net.HttpURLConnection, exception: java.io.IOException) {
            Logger.i("HttpURLConnection: intercepting exception")
            try {
                val header: String = connection.getRequestProperty(TRACKING_HEADER_KEY)
                val url: String = connection.url.toString()

                if (isAutoEnabled && hasTrackingHeader(header) && !isLibraryCallBoolean(url) && checkTag(header)) {
                    val marker = httpMarkers[header]
                    if (marker != null) {
                        marker.finish(connection, exception)
                        httpMarkers.remove(header)
                    }
                }
            }catch (e:Exception){
                e.instanaGenericExceptionFallbackHandler(classType = "UrlConnectionInstrumentation", at = "handleException with java.net.HttpURLConnection & java.io.IOException")
            }
        }
    }
}
