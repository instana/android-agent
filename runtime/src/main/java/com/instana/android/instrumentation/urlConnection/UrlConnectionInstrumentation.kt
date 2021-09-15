package com.instana.android.instrumentation.urlConnection

import android.util.Log
import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.checkTag
import com.instana.android.core.util.ConstantsAndUtil.isAutoEnabled
import com.instana.android.core.util.ConstantsAndUtil.isBlacklistedURL
import com.instana.android.core.util.ConstantsAndUtil.isLibraryCallBoolean
import com.instana.android.instrumentation.HTTPMarker
import java.util.concurrent.ConcurrentHashMap


class UrlConnectionInstrumentation {

    companion object {

        private val httpMarkers = ConcurrentHashMap<String, HTTPMarker>()

        @JvmStatic
        fun logEmpty() {
            Log.d("MIKEL", "logEmpty")
        }

        @JvmStatic
        fun logInstance(urlConnection: java.net.URLConnection) {
            Log.d("MIKEL", urlConnection.url.toString())
        }

        @JvmStatic
        fun openConnection(connection: java.net.URLConnection) {
            Log.i("MIKEL", "HttpURLConnection: intercepting openConnection")
            val header = connection.getRequestProperty(TRACKING_HEADER_KEY)
            val url = connection.url.toString()
            if (isAutoEnabled && !checkTag(header) && !isLibraryCallBoolean(url) && !isBlacklistedURL(url)) {
                val marker = Instana.startCapture(url)
                if (marker != null) {
                    connection.setRequestProperty(TRACKING_HEADER_KEY, marker.headerValue())
                    httpMarkers[marker.headerValue()] = marker
                }
            }
        }

        @JvmStatic
        fun disconnect(connection: java.net.HttpURLConnection) {
            Log.i("MIKEL", "HttpURLConnection: intercepting disconnect")
            val header = connection.getRequestProperty(TRACKING_HEADER_KEY)
            val url = connection.url.toString()
            if (isAutoEnabled && !isLibraryCallBoolean(url) && checkTag(header)) {
                httpMarkers[header]?.finish(connection)
                httpMarkers.remove(header)
            }
        }
    }
}
