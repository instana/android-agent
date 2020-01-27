package com.instana.android.core.event.worker

import android.content.Context
import androidx.work.*
import com.instana.android.Instana
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.JsonUtil
import com.instana.android.core.util.Logger
import com.instana.android.crash.CrashEventStore
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*

open class EventWorker(
        context: Context,
        private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {

        var eventsJson: String? = params.inputData.getString(EVENT_JSON_STRING)

        if (eventsJson == null || eventsJson.isEmpty()) {
            Result.failure()
        }

        if (eventsJson == CrashEventStore.tag) {
            // due to the crash string size we log just "crash"
            Logger.e("crash")
            eventsJson = CrashEventStore.json
            CrashEventStore.clear()
        } else {
            eventsJson?.let {
                Logger.e(it)
            }
        }

        var request: Request?
        eventsJson.let {

            request = Request.Builder()
                    .url("${Instana.configuration.reportingUrl}/${Instana.configuration.key}/batch")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept-Encoding", "gzip")
                    .post(RequestBody.create(JSON, it!!))
                    .build()
        }

        var response: Response? = null
        try {
            if (request == null) {
                Result.failure()
            }
            response = ConstantsAndUtil.client.newCall(request!!).execute()
            if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure()
        } finally {
            response?.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure()
    }

    companion object {

        fun createWorkRequest(
                constraints: Constraints,
                event: List<BaseEvent>,
                tag: String = UUID.randomUUID().toString()
        ): OneTimeWorkRequest {
            val serialized = JsonUtil.EVENT_JSON_ADAPTER.toJson(event)
            val data = Data.Builder().putString(EVENT_JSON_STRING, serialized).build()
            return OneTimeWorkRequest.Builder(EventWorker::class.java)
                    .setInputData(data)
                    .setConstraints(constraints)
                    .addTag(tag)
                    .build()
        }

        fun createCrashWorkRequest(
                constraints: Constraints,
                tag: String
        ): OneTimeWorkRequest = OneTimeWorkRequest.Builder(EventWorker::class.java)
                .setInputData(Data.Builder().putString(EVENT_JSON_STRING, tag).build())
                .setConstraints(constraints)
                .addTag(tag)
                .build()

        val JSON = MediaType.parse("application/json; charset=utf-8")

        const val EVENT_JSON_STRING = "event_string"
    }
}