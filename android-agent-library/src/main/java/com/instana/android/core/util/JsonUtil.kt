package com.instana.android.core.util

import android.content.Context
import androidx.annotation.RestrictTo
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.models.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.io.IOException

@RestrictTo(RestrictTo.Scope.LIBRARY)
object JsonUtil {

    private val moshi: Moshi = Moshi.Builder().build()

    val EVENT_JSON_ADAPTER: JsonAdapter<List<BaseEvent>> = EventsAdapter()

    val CONFIG_JSON_ADAPTER: JsonAdapter<InstanaConfiguration> = moshi.adapter(InstanaConfiguration::class.java)

    class EventsAdapter : JsonAdapter<List<BaseEvent>>() {

        private val crashAdapter: JsonAdapter<CrashEvent> by lazy {
            moshi.adapter(CrashEvent::class.java)
        }

        private val anrAlertAdapter: JsonAdapter<AnrAlertEvent> by lazy {
            moshi.adapter(AnrAlertEvent::class.java)
        }

        private val frameSkipAlertAdapter: JsonAdapter<FrameSkipAlertEvent> by lazy {
            moshi.adapter(FrameSkipAlertEvent::class.java)
        }

        private val lowMemoryAlertAdapter: JsonAdapter<LowMemoryAlertEvent> by lazy {
            moshi.adapter(LowMemoryAlertEvent::class.java)
        }

        private val customAdapter: JsonAdapter<CustomEvent> by lazy {
            moshi.adapter(CustomEvent::class.java)
        }

        private val remoteAdapter: JsonAdapter<RemoteCallEvent> by lazy {
            moshi.adapter(RemoteCallEvent::class.java)
        }

        private val sessionAdapter: JsonAdapter<SessionEvent> by lazy {
            moshi.adapter(SessionEvent::class.java)
        }

        override fun fromJson(reader: JsonReader): List<BaseEvent>? {
            return emptyList()
        }

        override fun toJson(writer: JsonWriter, value: List<BaseEvent>?) {
            writer.beginArray()
            value?.forEach {
                when (it) {
                    is AnrAlertEvent -> {
                        anrAlertAdapter.toJson(writer, it)
                    }
                    is FrameSkipAlertEvent -> {
                        frameSkipAlertAdapter.toJson(writer, it)
                    }
                    is LowMemoryAlertEvent -> {
                        lowMemoryAlertAdapter.toJson(writer, it)
                    }
                    is CrashEvent -> {
                        crashAdapter.toJson(writer, it)
                    }
                    is CustomEvent -> {
                        customAdapter.toJson(writer, it)
                    }
                    is RemoteCallEvent -> {
                        remoteAdapter.toJson(writer, it)
                    }
                    is SessionEvent -> {
                        sessionAdapter.toJson(writer, it)
                    }
                }
            }
            writer.endArray()
        }
    }

    fun getAssetJsonString(context: Context): InstanaConfiguration? {
        val configuration: InstanaConfiguration?
        try {
            val data = context.assets.open("instana-config.json")
            val size = data.available()
            val buffer = ByteArray(size)
            data.read(buffer)
            data.close()
            val json = String(buffer, Charsets.UTF_8)
            configuration = JsonUtil.CONFIG_JSON_ADAPTER.fromJson(json)
        } catch (ex: IOException) {
            return null
        }
        return configuration
    }
}