/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.event.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.instana.android.Instana
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

open class EventWorker(
    context: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val manager = Instana.workManager
        val isWorkWithoutApp = (manager == null)
        if (isWorkWithoutApp) {
            Logger.i("Do offline work now")
        }

        val directoryAbsPath: String? = params.inputData.getString(DIRECTORY_ABS_PATH)
        if (directoryAbsPath.isNullOrBlank()) {
            Logger.e("Tried to flush beacons with invalid directory path: $directoryAbsPath")
            return makeResult(manager, Result.failure())
        }

        var inSlowModeBeforeFlush = false
        var limit = maxBatchLimit
        if (!isWorkWithoutApp) {
            inSlowModeBeforeFlush = manager!!.isInSlowSendMode()
            if (inSlowModeBeforeFlush) {
                limit = 1
            }
        }

        val directory = File(directoryAbsPath)
        val (data, files) = readAllFiles(manager, directory, limit)
        if (data.isBlank()) {
            return makeResult(manager, Result.success())
        }

        val sendRet = send(data)
        if (!isWorkWithoutApp && manager!!.sendFirstBeacon) {
            manager.sendFirstBeacon = false
        }

        return when {
            sendRet -> {
                files.forEach { it.delete() }
                Logger.i("Beacon-batch sent with: `size` ${files.size}")
                if (isWorkWithoutApp) {
                    if (files.size == limit)
                        Result.retry()
                    else
                        Result.success()
                } else {
                    if (manager!!.isInSlowSendMode()) {
                        manager.slowSendStartTime = null  // not in slow send mode anymore
                    }
                    val result = makeResult(manager, Result.success())
                    scheduleFlushAgain(manager, inSlowModeBeforeFlush, files.size == limit)
                    result
                }
            }

            else -> {
                if (isWorkWithoutApp) {
                    if (params.inputData.getBoolean(ALLOW_SLOW_SEND, false)) {
                        Result.failure()
                    } else {
                        Result.retry()
                    }
                } else {
                    if (manager!!.canDoSlowSend()) {
                        manager.slowSendStartTime = System.currentTimeMillis()
                        val result = makeResult(manager, Result.success())
                        scheduleFlushAgain(manager, inSlowModeBeforeFlush)
                        result
                    } else {
                        makeResult(manager, Result.retry())
                    }
                }
            }
        }
    }

    private fun makeResult(manager: InstanaWorkManager?, result: Result): Result {
        if (manager != null) {
            manager.lastFlushTimeMillis.set(0)
            Logger.d("makeResult() lastFlushTimeMillis set to 0")
        }
        return result
    }

    private fun scheduleFlushAgain(
        instanaManager: InstanaWorkManager,
        inSlowModeBeforeFlush: Boolean,
        reachedBatchLimit: Boolean = false,
    ) {
        val workManager = instanaManager.getWorkManager()
        if (workManager == null) {
            Logger.w("Empty WorkManager, can not reschedule flush, reached batch limit is $reachedBatchLimit, in slow mode before flush is $inSlowModeBeforeFlush")
            return
        }
        workManager.run {
            // Another flush either send 1 beacon (currently in slow mode)
            // or flush next batch of beacons (just got out of slow send mode)
            // or simply send next batch of beacons (reachedBatchLimit is true)
            val msg = if (instanaManager.isInSlowSendMode()) {
                "schedule flush to send 1 beacon in slow send mode"
            } else if (inSlowModeBeforeFlush) {
                "schedule to flush next batch of beacons after out of slow send mode"
            } else if (reachedBatchLimit) {
                "Detected more beacons in queue. Creating a new beacon-batch"
            } else {
                ""
            }
            if (msg.isNotBlank()) {
                Logger.i(msg)
                instanaManager.flush(this)
            }
        }
    }

    private fun readAllFiles(
        instanaManager: InstanaWorkManager?,
        directory: File, limit: Int,
    ): Pair<String, Array<File>> {
        val files = directory.listFiles() ?: emptyArray()
        val filteredFileList = staleBeaconsRemover(files)
        val sb = StringBuffer()
        var retFiles: Array<File> = arrayOf()
        val meta = instanaManager?.slowSendStartTime?.toString()
        filteredFileList.take(limit).forEach {
            var beaconStr = it.readText(Charsets.UTF_8)
            if (meta != null) {
                beaconStr = Beacon.addMetaData(beaconStr, "slowSendStartTime", meta)
            }
            sb.append("$beaconStr\n")
            retFiles += it
        }
        return sb.toString() to retFiles
    }

    /**
     * @return true when beacon was handled (so it can be discarded), false when it wasn't
     */
    private fun send(data: String): Boolean {
        val reportingURL = params.inputData.getString(REPORTING_URL)
        if (reportingURL.isNullOrBlank()) {
            Logger.w("Instana hasn't been initialized. Dropping beacon.")
            return true
        }
        return try {
            val requestBody = RequestBody.create(TEXT_PLAIN, data)
            val request = Request.Builder()
                .url(reportingURL)
                .addHeader("Content-Type", "application/json") // TODO ??? it's def not json
                .addHeader("Accept-Encoding", "gzip")
                .post(requestBody)
                .build()
            val response = ConstantsAndUtil.client.newCall(request).execute()
            if (response.isSuccessful) {
                // success
            } else if (response.code() == 400) {
                // unknown key, delete beacon otherwise it will block new beacons
                Logger.e("Failed to flush beacons to Instana with: Unknown key. reportingURL '$reportingURL', errorMessage '${response.message()}'")
                return true
            } else {
                Logger.e("Failed to flush beacons to Instana with: reportingURL '$reportingURL', responseCode '${response.code()}', errorMessage '${response.message()}'")
            }
            response.isSuccessful
        } catch (e: IOException) {
            Logger.e("Failed to flush beacons to Instana, errorMessage: ${e.message} ",e)
            false
        }
    }

    companion object {

        private const val maxBatchLimit = 100

        fun createWorkRequest(
            constraints: Constraints,
            directory: File,
            reportingURL: String?,
            allowSlowSend: Boolean,
            initialDelayMs: Long,
            tag: String,
        ): OneTimeWorkRequest {
            val data = Data.Builder()
                .putString(DIRECTORY_ABS_PATH, directory.absolutePath)
                .putString(REPORTING_URL, reportingURL)
                .putBoolean(ALLOW_SLOW_SEND, allowSlowSend)
                .build()
            return OneTimeWorkRequest.Builder(EventWorker::class.java)
                .setInputData(data)
                .setConstraints(constraints)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .build()
        }

        private val TEXT_PLAIN = MediaType.parse("text/plain; charset=utf-8")

        private const val DIRECTORY_ABS_PATH = "dir_abs_path"
        private const val REPORTING_URL = "reporting_url"
        private const val ALLOW_SLOW_SEND = "allow_slow_mode"
    }

    private fun staleBeaconsRemover(files: Array<File>): Array<File> {
        val maxStaleBeaconLimit = 1000 //Should never go below 100
        val fileSize = files.size
        return if (fileSize > 3 * maxStaleBeaconLimit) {
            // If total number exceeds 3 * max limit, delete all files
            Logger.i("Dropping all beacons as limit exceeds 3 times the allowed limit")
            files.forEach(File::delete)
            emptyArray()
        } else if (fileSize in maxStaleBeaconLimit until 3 * maxStaleBeaconLimit) {
            // Keep the most recent (maxStaleBeaconLimit-maxBatchLimit) files
            val recentFiles = files.sortedByDescending(File::lastModified).take(maxStaleBeaconLimit - maxBatchLimit)
            // Delete the rest of the files other than latest
            Logger.i("keeping recent beacons and dropping older ones")
            files.filterNot { it in recentFiles }.forEach(File::delete)
            recentFiles.toTypedArray()
        } else {
            files
        }
    }


}
