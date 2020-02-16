package com.instana.android.core.event.worker

import android.content.Context
import androidx.work.*
import com.instana.android.Instana
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.util.ConstantsAndUtil
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

        Result.success()

//        var eventsJson: String? = params.inputData.getString(EVENT_JSON_STRING)
//
//        if (eventsJson == null || eventsJson.isEmpty()) {
//            Result.failure()
//        }
//
//        if (eventsJson == CrashEventStore.tag) {
//            // due to the crash string size we log just "crash"
//            Logger.e("crash")
//            eventsJson = CrashEventStore.serialized
//            CrashEventStore.clear()
//        } else {
//            eventsJson?.let {
//                Logger.e(it)
//            }
//        }
//
//        var request: Request?
//        eventsJson.let {
//            request = Request.Builder()
//                .url(Instana.configuration.reportingUrl)
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Accept-Encoding", "gzip")
//                .post(RequestBody.create(TEXT_PLAIN, it!!))
//                .build()
//        }
//
//        var response: Response? = null
//        try {
//            if (request == null) {
//                Result.failure()
//            }
//            response = ConstantsAndUtil.client.newCall(request!!).execute()
//            if (response.code() == HttpURLConnection.HTTP_OK) {
//                Result.success()
//            } else {
//                Result.failure()
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Result.failure()
//        } finally {
//            response?.close()
//        }
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
            // TODO move serialization to a more sensible place
            val sb = StringBuffer()
            event.forEach { sb.append("${it.serialize()}\n") }
            val serialized = sb.toString()

            val data = Data.Builder().putString(EVENT_JSON_STRING, serialized).build()
            return OneTimeWorkRequest.Builder(EventWorker::class.java)
                .setInputData(data)
                .setConstraints(constraints)
                .addTag(tag)
                .build()
        }

        fun createWorkRequest2(
            constraints: Constraints,
            event: List<Beacon>,
            tag: String = UUID.randomUUID().toString()
        ): OneTimeWorkRequest {
            // TODO move serialization to a more sensible place
            val sb = StringBuffer()
            event.forEach { sb.append("$it\n") }
            val serialized = sb.toString()

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
        val TEXT_PLAIN = MediaType.parse("text/plain; charset=utf-8")

        const val EVENT_JSON_STRING = "event_string"
    }
}