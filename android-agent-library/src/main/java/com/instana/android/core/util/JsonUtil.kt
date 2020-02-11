package com.instana.android.core.util

import android.content.Context
import androidx.annotation.RestrictTo
import com.instana.android.core.InstanaConfiguration
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.IOException

@RestrictTo(RestrictTo.Scope.LIBRARY)
object JsonUtil {

    private val moshi: Moshi = Moshi.Builder().build()

    val CONFIG_JSON_ADAPTER: JsonAdapter<InstanaConfiguration> = moshi.adapter(InstanaConfiguration::class.java)

    fun getAssetJsonString(context: Context): InstanaConfiguration? {
        val configuration: InstanaConfiguration?
        try {
            val data = context.assets.open("instana-config.json")
            val size = data.available()
            val buffer = ByteArray(size)
            data.read(buffer)
            data.close()
            val json = String(buffer, Charsets.UTF_8)
            configuration = CONFIG_JSON_ADAPTER.fromJson(json)
        } catch (ex: IOException) {
            return null
        }
        return configuration
    }
}