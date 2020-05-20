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
        if (directoryAbsPath.isNullOrBlank()) {
            Logger.e("Tried to flush beacons with invalid directory path: $directoryAbsPath")
            return Result.failure()
        }

        val directory = File(directoryAbsPath)
        val (data, files) = readAllFiles(directory, batchLimit)
        return when {
            data.isBlank() -> Result.success()
            send(data) -> {
                files.forEach { it.delete() }
                if (files.size == batchLimit) {
                    Logger.i("Beacon-batch reached limit. Will create a new batch")
                    Result.retry()
                } else {
                    Logger.i("Beacon-batch sent with: `size` ${files.size}")
                    Result.success()
                }
            }
            else -> Result.retry()
        }
    }

    private fun readAllFiles(directory: File, limit: Int): Pair<String, Array<File>> {
        val files = directory.listFiles() ?: emptyArray()
        val sb = StringBuffer()
        files.take(limit).forEach { sb.append("${it.readText(Charsets.UTF_8)}\n") }
        return sb.toString() to files
    }

    private fun send(data: String): Boolean {
        val reportingURL = Instana.config?.reportingURL
        if (reportingURL == null) {
            Logger.w("Instana hasn't been initialized. Dropping beacon.")
            return true
        }
        return try {
            val request = Request.Builder()
                .url(reportingURL)
                .addHeader("Content-Type", "application/json") // TODO ??? it's def not json
                .addHeader("Accept-Encoding", "gzip")
                .post(data.toRequestBody(TEXT_PLAIN))
                .build()
            val response = ConstantsAndUtil.client.newCall(request).execute()
            response.isSuccessful
        } catch (e: IOException) {
            Logger.e("Failed to flush beacons to Instana", e)
            false
        }
    }

    companion object {

        private const val batchLimit = 100

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