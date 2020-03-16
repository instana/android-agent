/*
* Created by Mikel Pascual (mikel@4rtstudio.com) on 13/03/2020.
*/
package com.instana.mobileeum.network

import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


object HttpURLConnection {

    /**
     * Can't be run on Main Thread
     */
    fun executeRequest(method: String, url: String, body: String?): String {
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = (URL(url).openConnection() as HttpURLConnection).apply {
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Accept-Encoding", "gzip,deflate")
                requestMethod = method
                if (body != null) doOutput = true
            }
            urlConnection.connect()

            if (body != null) {
                urlConnection.outputStream.use { out ->
                    OutputStreamWriter(out, Charset.defaultCharset()).use { writer ->
                        writer.write(body)
                    }
                }
            }

            return urlConnection.toString()
        } catch (e: IOException) {
            return e.toString()
        } finally {
            urlConnection?.disconnect()
        }
    }
}
