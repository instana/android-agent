package com.instana.android.core.event.worker

import android.content.Context
import androidx.work.*
import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.File
import java.util.concurrent.TimeUnit

open class EventWorker(
    context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val directoryAbsPath: String? = params.inputData.getString(DIRECTORY_ABS_PATH)
        if (directoryAbsPath.isNullOrBlank()) return Result.failure()

        val directory = File(directoryAbsPath)
        val (data, files) = readAllFiles(directory)
        return when {
            data.isBlank() -> Result.success()
            send(data) -> {
                files.forEach { it.delete() }
                Result.success()
            }
            else -> Result.retry()
        }
    }

    private fun readAllFiles(directory: File): Pair<String, Array<File>> {
        val files = directory.listFiles() ?: emptyArray()
        val sb = StringBuffer()
        files.forEach { sb.append("${it.readText(Charsets.UTF_8)}\n") }
        return sb.toString() to files
    }

    private fun send(data: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(Instana.config.reportingURL)
                .addHeader("Content-Type", "application/json") // TODO ??? it's def not json
                .addHeader("Accept-Encoding", "gzip")
                .post(data.toRequestBody(TEXT_PLAIN))
                .build()
            val response = ConstantsAndUtil.client.newCall(request).execute()
            response.isSuccessful
        } catch (e: IOException) {
            Logger.e("Failed to send pending beacons to Instana: ${e.message}")
            false
        }
    }

    companion object {

        fun createWorkRequest(
            constraints: Constraints,
            directory: File,
            initialDelayMs: Long,
            tag: String
        ): OneTimeWorkRequest {
            val data = Data.Builder()
                .putString(DIRECTORY_ABS_PATH, directory.absolutePath)
                .build()
            return OneTimeWorkRequest.Builder(EventWorker::class.java)
                .setInputData(data)
                .setConstraints(constraints)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .build()
        }

        private val TEXT_PLAIN = "text/plain; charset=utf-8".toMediaTypeOrNull()

        private const val DIRECTORY_ABS_PATH = "dir_abs_path"
    }
}