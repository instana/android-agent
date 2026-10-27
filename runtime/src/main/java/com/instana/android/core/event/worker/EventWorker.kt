/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.event.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.instana.android.Instana
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.worker.EventWorker.Companion.STAGING_RECOVERY_THRESHOLD_MS
import com.instana.android.core.util.BeaconSendEventListener
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import com.instana.android.core.util.beaconFileFromInflight
import com.instana.android.core.util.bumpRetryCount
import com.instana.android.core.util.isCrashBeacon
import com.instana.android.core.util.isInflightMarker
import com.instana.android.core.util.retryCount
import com.instana.android.core.util.toInflightFile
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
        // Each worker run is tagged with a short unique id so concurrent log lines can be distinguished.
        val workerTag = id.toString().takeLast(8)
        Logger.i("[INSTANA_DEDUP] worker=$workerTag doWork() START runAttempt=$runAttemptCount")

        val directoryAbsPath: String? = params.inputData.getString(DIRECTORY_ABS_PATH)
        if (directoryAbsPath.isNullOrBlank()) {
            Logger.e("[INSTANA_DEDUP] worker=$workerTag invalid directory path: $directoryAbsPath")
            return finaliseResult(manager, Result.failure(), workerTag, "invalid-dir")
        }

        val stagingAbsPath: String? = params.inputData.getString(STAGING_DIRECTORY_ABS_PATH)
        val beaconsDir = File(directoryAbsPath)
        val stagingDir = if (!stagingAbsPath.isNullOrBlank()) File(stagingAbsPath) else null

        val strictDedup = params.inputData.getBoolean(
            ENABLE_STRICT_BEACON_DEDUP,
            Instana.config?.enableStrictBeaconDedup ?: false
        )

        // Recover any files left in staging from a PREVIOUS crashed/killed worker.
        // Files younger than HTTP_TIMEOUT_SECONDS+5s are owned by an ACTIVE worker — leave them.
        if (stagingDir != null) {
            recoverStagingFiles(stagingDir, beaconsDir, workerTag, strictDedup)
        }

        var inSlowModeBeforeFlush = false
        var limit = maxBatchLimit
        if (!isWorkWithoutApp) {
            inSlowModeBeforeFlush = manager!!.isInSlowSendMode()
            if (inSlowModeBeforeFlush) limit = 1
        }

        val (data, files) = readAllFiles(manager, beaconsDir, stagingDir, limit, workerTag)
        if (data.isBlank()) {
            return finaliseResult(manager, Result.success(), workerTag, "no-data")
        }

        // Mark all claimed staging files as inflight BEFORE calling execute() so that if the
        // process is killed mid-POST, recoverStagingFiles() in the next process can distinguish
        // "POST was started" (.inflight) from "POST not yet started" (plain name).
        val inflightFiles: Array<File> = if (stagingDir != null) {
            files.map { f ->
                try {
                    val inf = f.toInflightFile()
                    if (f.renameTo(inf)) inf
                    else {
                        Logger.w("[INSTANA_DEDUP] worker=$workerTag INFLIGHT rename FAILED for ${f.name} — keeping original")
                        f
                    }
                } catch (e: Exception) {
                    Logger.e("[INSTANA_DEDUP] worker=$workerTag INFLIGHT rename error for ${f.name}", e)
                    f
                }
            }.toTypedArray()
        } else {
            files
        }

        val sendResult = send(data, workerTag)
        Logger.i("[INSTANA_DEDUP] worker=$workerTag send() result: success=${sendResult.success} bodyFullySent=${sendResult.bodyFullySent}")

        return when {
            sendResult.success -> {
                // Success: delete inflight files BEFORE resetting the scheduling gate so that
                // any worker triggered immediately after cannot recover these files.
                inflightFiles.forEach { f ->
                    try { f.delete() } catch (e: Exception) {
                        Logger.e("[INSTANA_DEDUP] worker=$workerTag failed to delete sent beacon: ${f.name}", e)
                    }
                }
                Logger.i("[INSTANA_DEDUP] worker=$workerTag send SUCCESS — sent=${inflightFiles.size}")

                if (isWorkWithoutApp) {
                    if (inflightFiles.size == limit) Result.retry() else Result.success()
                } else {
                    if (manager!!.isInSlowSendMode()) {
                        manager.slowSendStartTime = null  // exit slow-send mode
                    }
                    // Reset gate and optionally schedule next batch AFTER deletes are done.
                    val result = finaliseResult(manager, Result.success(), workerTag, "success")
                    scheduleFlushAgain(manager, inSlowModeBeforeFlush, inflightFiles.size == limit, workerTag)
                    result
                }
            }

            sendResult.bodyFullySent && strictDedup -> {
                // Strict mode: Body fully delivered to server socket before IOException (response lost).
                // Delete inflight files; do NOT retry — server already has the data.
                inflightFiles.forEach { f ->
                    try { f.delete() } catch (e: Exception) {
                        Logger.e("[INSTANA_DEDUP] worker=$workerTag failed to delete body-sent beacon: ${f.name}", e)
                    }
                }
                Logger.i("[INSTANA_DEDUP] worker=$workerTag body fully sent before IOException (strict dedup) — treating as success, not retrying")
                finaliseResult(manager, Result.success(), workerTag, "body-sent-success")
            }

            else -> {
                // Failure, or unconfirmed response in non-strict mode: rename .inflight back to
                // plain name, bump retry counter, or drop if retry budget exhausted. Move files back to beaconsDir.
                var anyFileKept = false
                if (stagingDir != null) {
                    inflightFiles.forEach { inflightFile ->
                        // Strip .inflight suffix to recover the plain staging name (preserves _rN).
                        val stagingFile: File = if (inflightFile.isInflightMarker()) {
                            try {
                                val plain = inflightFile.beaconFileFromInflight()
                                if (inflightFile.renameTo(plain)) plain
                                else {
                                    Logger.w("[INSTANA_DEDUP] worker=$workerTag failed to rename back ${inflightFile.name}")
                                    inflightFile
                                }
                            } catch (e: Exception) {
                                Logger.e("[INSTANA_DEDUP] worker=$workerTag error renaming back ${inflightFile.name}", e)
                                inflightFile
                            }
                        } else {
                            inflightFile
                        }
                        val retryLimit = when {
                            stagingFile.isCrashBeacon() -> MAX_CRASH_RETRIES
                            sendResult.bodyFullySent || !strictDedup -> MAX_INFLIGHT_RETRIES
                            else -> MAX_SEND_RETRIES
                        }
                        val currentRetry = stagingFile.retryCount()
                        if (currentRetry >= retryLimit) {
                            Logger.i("[INSTANA_DEDUP] worker=$workerTag DROP beacon retries=$currentRetry/$retryLimit file=${stagingFile.name}")
                            try { stagingFile.delete() } catch (e: Exception) {
                                Logger.e("[INSTANA_DEDUP] worker=$workerTag failed to delete expired beacon: ${stagingFile.name}", e)
                            }
                        } else {
                            try {
                                val bumped = stagingFile.bumpRetryCount()
                                val dest = File(beaconsDir, bumped.name)
                                if (!stagingFile.renameTo(dest)) {
                                    Logger.w("[INSTANA_DEDUP] worker=$workerTag failed to move staging file back: ${stagingFile.name}")
                                    try { stagingFile.delete() } catch (de: Exception) {
                                        Logger.e("[INSTANA_DEDUP] worker=$workerTag failed to delete stuck staging file: ${stagingFile.name}", de)
                                    }
                                } else {
                                    anyFileKept = true
                                }
                            } catch (e: Exception) {
                                Logger.e("[INSTANA_DEDUP] worker=$workerTag error bumping retry for: ${stagingFile.name}, deleting", e)
                                try { stagingFile.delete() } catch (de: Exception) {
                                    Logger.e("[INSTANA_DEDUP] worker=$workerTag failed to delete stuck staging file: ${stagingFile.name}", de)
                                }
                            }
                        }
                    }
                } else {
                    anyFileKept = inflightFiles.isNotEmpty()
                }

                if (isWorkWithoutApp) {
                    if (!anyFileKept) {
                        Result.failure()
                    } else if (params.inputData.getBoolean(ALLOW_SLOW_SEND, false)) {
                        Result.failure()
                    } else {
                        Result.retry()
                    }
                } else {
                    if (!anyFileKept) {
                        finaliseResult(manager, Result.failure(), workerTag, "all-dropped")
                    } else if (manager!!.canDoSlowSend()) {
                        manager.slowSendStartTime = System.currentTimeMillis()
                        val result = finaliseResult(manager, Result.success(), workerTag, "slow-send")
                        scheduleFlushAgain(manager, inSlowModeBeforeFlush, workerTag = workerTag)
                        result
                    } else {
                        finaliseResult(manager, Result.retry(), workerTag, "retry")
                    }
                }
            }
        }
    }

    /**
     * Resets the scheduling gate and returns [result].
     *
     * Gate reset is deferred until AFTER all staging file operations (deletes / renames) are
     * complete. This prevents a concurrently-triggered worker from opening the gate while files
     * still exist in staging, which would allow it to recover and re-POST those files.
     */
    private fun finaliseResult(
        manager: InstanaWorkManager?,
        result: Result,
        workerTag: String,
        reason: String,
    ): Result {
        if (manager != null) {
            manager.lastFlushTimeMillis.set(0)
            Logger.d("[INSTANA_DEDUP] worker=$workerTag finaliseResult reason=$reason")
        }
        return result
    }

    private fun scheduleFlushAgain(
        instanaManager: InstanaWorkManager,
        inSlowModeBeforeFlush: Boolean,
        reachedBatchLimit: Boolean = false,
        workerTag: String = "",
    ) {
        val workManager = instanaManager.getWorkManager()
        if (workManager == null) {
            Logger.w("[INSTANA_DEDUP] worker=$workerTag scheduleFlushAgain: WorkManager null, cannot reschedule")
            return
        }
        workManager.run {
            val shouldReschedule = instanaManager.isInSlowSendMode() || inSlowModeBeforeFlush || reachedBatchLimit
            if (shouldReschedule) {
                instanaManager.flush(this)
            }
        }
    }

    private fun readAllFiles(
        instanaManager: InstanaWorkManager?,
        directory: File,
        stagingDir: File?,
        limit: Int,
        workerTag: String = "",
    ): Pair<String, Array<File>> {
        val files = directory.listFiles() ?: emptyArray()
        var filteredFileList = staleBeaconsRemover(files)
        if (Instana.config?.deleteOldBeacons == true) {
            filteredFileList = removeOldFiles(filteredFileList)
        }
        val sb = StringBuffer()
        var retFiles: Array<File> = arrayOf()
        val meta = instanaManager?.slowSendStartTime?.toString()
        // Sort oldest-first so session-start beacons (created first) are always delivered
        // first and are never expired while arbitrarily newer beacons are sent.
        filteredFileList = filteredFileList.sortedBy(File::lastModified).toTypedArray()
        filteredFileList.take(limit).forEach { file ->
            // Atomically claim the file by moving it to staging before reading.
            // Any concurrent EventWorker listing the beaconsDir will not see this file.
            val claimedFile: File = try {
                if (stagingDir != null) {
                    val dest = File(stagingDir, file.name)
                    if (file.renameTo(dest)) dest
                    else {
                        // Another worker already claimed this file — expected outcome of atomic-claim design.
                        Logger.w("[INSTANA_DEDUP] worker=$workerTag CLAIM FAILED (already taken?) ${file.name} — skipping")
                        return@forEach
                    }
                } else {
                    file
                }
            } catch (e: Exception) {
                Logger.e("[INSTANA_DEDUP] worker=$workerTag CLAIM ERROR ${file.name}", e)
                return@forEach
            }
            val beaconStr: String = try {
                claimedFile.readText(Charsets.UTF_8)
            } catch (e: Exception) {
                Logger.e("[INSTANA_DEDUP] worker=$workerTag READ ERROR ${claimedFile.name}", e)
                if (stagingDir != null) {
                    try { claimedFile.renameTo(File(directory, claimedFile.name)) }
                    catch (re: Exception) { Logger.e("[INSTANA_DEDUP] worker=$workerTag failed to move unreadable file back", re) }
                }
                return@forEach
            }
            val finalStr = if (meta != null) {
                Beacon.addMetaData(beaconStr, "slowSendStartTime", meta)
            } else {
                beaconStr
            }
            sb.append("$finalStr\n")
            retFiles += claimedFile
        }
        Logger.i("[INSTANA_DEDUP] worker=$workerTag readAllFiles: claimed ${retFiles.size} beacon(s)")
        return sb.toString() to retFiles
    }

    /**
     * Handles stale files left in [stagingDir] at the start of every doWork().
     *
     * **Three-state recovery policy (inflight-marker design):**
     *
     * | File name             | Meaning                                   | Action                          |
     * |---|---|---|
     * | `<id>`                | Claimed but HTTP POST **not yet started** | Move back to beaconsDir (retry) |
     * | `<id>.inflight`       | POST **was started**, outcome unknown     | Delete (prevents duplicate)     |
     * | `cr_<id>` (either)    | Crash beacon, any state                   | Always recover to beaconsDir    |
     *
     * Crash beacons are always recovered regardless of inflight state because the backend
     * deduplicates crashes by `stackTraceKeyChecksum`, so a duplicate crash report is
     * far less harmful than silently losing a crash.
     *
     * Files younger than [STAGING_RECOVERY_THRESHOLD_MS] belong to an ACTIVE worker in
     * the current process — leave them untouched.
     *
     * All operations are wrapped in try/catch — a failure here must never crash the host app.
     */
    internal fun recoverStagingFiles(
        stagingDir: File,
        beaconsDir: File,
        workerTag: String = "",
        strictDedup: Boolean = false,
    ) {
        try {
            val stagingFiles = stagingDir.listFiles() ?: return
            val now = System.currentTimeMillis()
            var deleted = 0
            var recovered = 0
            for (file in stagingFiles) {
                try {
                    val ageMs = now - file.lastModified()
                    if (ageMs < STAGING_RECOVERY_THRESHOLD_MS) continue  // active worker owns this file

                    // File is old enough that its worker process is certainly dead.
                    // Determine the plain beacon name (strip .inflight if present) for crash check.
                    val plainFile: File = if (file.isInflightMarker()) file.beaconFileFromInflight() else file

                    if (plainFile.isCrashBeacon()) {
                        // Crash beacons: always recover regardless of inflight state.
                        // Backend deduplicates crashes by stackTraceKeyChecksum.
                        val dest = File(beaconsDir, plainFile.name)
                        val moved = try { file.renameTo(dest) } catch (e: Exception) { false }
                        if (moved) recovered++
                        else Logger.w("[INSTANA_DEDUP] worker=$workerTag failed to recover crash beacon: ${file.name}")
                        continue
                    }

                    if (file.isInflightMarker()) {
                        if (strictDedup) {
                            // Inflight non-crash (strict mode): POST was started, outcome unknown — DELETE to prevent duplicate.
                            if (file.delete()) deleted++
                            else Logger.w("[INSTANA_DEDUP] worker=$workerTag failed to delete stale inflight file: ${file.name}")
                        } else {
                            // Non-strict mode: retry up to MAX_INFLIGHT_RETRIES (3)
                            val currentRetry = plainFile.retryCount()
                            if (currentRetry >= MAX_INFLIGHT_RETRIES) {
                                Logger.i("[INSTANA_DEDUP] worker=$workerTag DROP stale inflight beacon retries=$currentRetry/$MAX_INFLIGHT_RETRIES file=${plainFile.name}")
                                if (file.delete()) deleted++
                                else Logger.w("[INSTANA_DEDUP] worker=$workerTag failed to delete retry-exhausted stale inflight file: ${file.name}")
                            } else {
                                val bumped = plainFile.bumpRetryCount()
                                val dest = File(beaconsDir, bumped.name)
                                val moved = try {
                                    file.renameTo(dest)
                                } catch (e: Exception) {
                                    false
                                }
                                if (moved) recovered++
                                else Logger.w("[INSTANA_DEDUP] worker=$workerTag failed to recover stale inflight file: ${file.name}")
                            }
                        }
                    } else {
                        // Plain (non-inflight) non-crash: POST was never started — safe to retry.
                        val dest = File(beaconsDir, file.name)
                        val moved = try { file.renameTo(dest) } catch (e: Exception) { false }
                        if (moved) recovered++
                        else Logger.w("[INSTANA_DEDUP] worker=$workerTag failed to recover plain beacon: ${file.name}")
                    }
                } catch (e: Exception) {
                    Logger.e("[INSTANA_DEDUP] worker=$workerTag error handling stale staging file: ${file.name}", e)
                }
            }
            Logger.i("[INSTANA_DEDUP] worker=$workerTag recoverStagingFiles: deleted(inflight)=$deleted recovered=$recovered")
        } catch (e: Exception) {
            Logger.e("[INSTANA_DEDUP] worker=$workerTag failed to list staging directory during recovery, continuing", e)
        }
    }

    /**
     * Result of a single [send] attempt.
     *
     * @param success true when the server responded with 2xx (beacon accepted).
     * @param bodyFullySent true when [requestBodyEnd][okhttp3.EventListener.requestBodyEnd]
     *   fired before any IOException, meaning the full payload reached the server's Kafka
     *   topic even if the response was lost. When true and success is false, staging files
     *   must be deleted rather than retried to avoid duplicate beacons.
     */
    internal data class SendResult(val success: Boolean, val bodyFullySent: Boolean)

    /**
     * @return [SendResult] describing whether the call succeeded and whether the full body
     *   was delivered to the server's socket before any failure.
     */
    private fun send(data: String, workerTag: String = ""): SendResult {
        val reportingURL = params.inputData.getString(REPORTING_URL)
        if (reportingURL.isNullOrBlank()) {
            return SendResult(success = true, bodyFullySent = true)
        }

        // One BeaconSendEventListener per send() call — local variable, zero shared state.
        val bodySentListener = BeaconSendEventListener()
        val callClient = ConstantsAndUtil.client.newBuilder()
            .eventListener(bodySentListener)
            .build()

        val request = Request.Builder()
            .url(reportingURL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept-Encoding", "gzip")
            .post(RequestBody.create(TEXT_PLAIN, data))
            .build()

        return try {
            val response = callClient.newCall(request).execute()
            val code = response.code()
            if (code == 400) {
                Logger.e("[INSTANA_DEDUP] worker=$workerTag send: 400 bad key — treating as success to unblock queue")
                return SendResult(success = true, bodyFullySent = true)
            }
            if (!response.isSuccessful) {
                Logger.e("[INSTANA_DEDUP] worker=$workerTag send: non-2xx response code=$code")
            }
            SendResult(success = response.isSuccessful, bodyFullySent = response.isSuccessful)
        } catch (e: IOException) {
            val bodySent = bodySentListener.requestBodySent.get()
            Logger.e("[INSTANA_DEDUP] worker=$workerTag send: IOException bodySent=$bodySent", e)
            SendResult(success = false, bodyFullySent = bodySent)
        }
    }

    companion object {

        private const val maxBatchLimit = 100

        // In-flight beacons with unknown response (non-strict mode): max 3 attempts then drop.
        internal const val MAX_INFLIGHT_RETRIES = 3

        // Normal beacons: max 5 attempts then drop.
        // With EXPONENTIAL 60s backoff: ~1min→2min→4min→8min→16min ≈ 31 min total window.
        internal const val MAX_SEND_RETRIES = 5

        // Crash beacons (cr_ prefix): max 10 attempts before drop.
        internal const val MAX_CRASH_RETRIES = 10

        /**
         * Staging files younger than this threshold are owned by an active worker and must NOT
         * be recovered. Value = HTTP_TIMEOUT_SECONDS (30s) + 5s buffer = 35 000 ms.
         */
        internal val STAGING_RECOVERY_THRESHOLD_MS: Long =
            (ConstantsAndUtil.HTTP_TIMEOUT_SECONDS + 5L) * 1000L // 35 000 ms

        fun createWorkRequest(
            constraints: Constraints,
            directory: File,
            stagingDirectory: File?,
            reportingURL: String?,
            allowSlowSend: Boolean,
            initialDelayMs: Long,
            tag: String,
            enableStrictBeaconDedup: Boolean = false,
        ): OneTimeWorkRequest {
            val dataBuilder = Data.Builder()
                .putString(DIRECTORY_ABS_PATH, directory.absolutePath)
                .putString(REPORTING_URL, reportingURL)
                .putBoolean(ALLOW_SLOW_SEND, allowSlowSend)
                .putBoolean(ENABLE_STRICT_BEACON_DEDUP, enableStrictBeaconDedup)
            if (stagingDirectory != null) {
                dataBuilder.putString(STAGING_DIRECTORY_ABS_PATH, stagingDirectory.absolutePath)
            }
            // Backoff: normal beacons ~1min→2min→4min (3 attempts); crash beacons up to 10
            // attempts capped at ~5h per WorkManager's 18,000s maximum for EXPONENTIAL policy.
            return OneTimeWorkRequest.Builder(EventWorker::class.java)
                .setInputData(dataBuilder.build())
                .setConstraints(constraints)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 60, TimeUnit.SECONDS)
                .addTag(tag)
                .build()
        }

        private val TEXT_PLAIN = MediaType.parse("text/plain; charset=utf-8")

        private const val DIRECTORY_ABS_PATH = "dir_abs_path"
        private const val STAGING_DIRECTORY_ABS_PATH = "staging_dir_abs_path"
        private const val REPORTING_URL = "reporting_url"
        private const val ALLOW_SLOW_SEND = "allow_slow_mode"
        private const val ENABLE_STRICT_BEACON_DEDUP = "enable_strict_beacon_dedup"
    }

    private fun staleBeaconsRemover(files: Array<File>): Array<File> {
        val maxStaleBeaconLimit = 1000 //Should never go below 100
        val keepCount = maxStaleBeaconLimit - maxBatchLimit // 900
        val fileSize = files.size
        return if (fileSize > 3 * maxStaleBeaconLimit) {
            // File count exceeds 3× the limit. Keep the 900 most-recent files (same policy
            // as the 1000–3000 band) rather than deleting everything — avoids losing all
            // buffered data on a single bad flush cycle.
            val recentFiles = files.sortedByDescending(File::lastModified).take(keepCount)
            Logger.i("Beacon count ($fileSize) exceeds 3× limit. Keeping $keepCount most-recent, dropping rest.")
            files.filterNot { it in recentFiles }.forEach(File::delete)
            recentFiles.toTypedArray()
        } else if (fileSize in maxStaleBeaconLimit until 3 * maxStaleBeaconLimit) {
            // Keep the most recent (maxStaleBeaconLimit-maxBatchLimit) files
            val recentFiles = files.sortedByDescending(File::lastModified).take(keepCount)
            // Delete the rest of the files other than latest
            Logger.i("keeping recent beacons and dropping older ones")
            files.filterNot { it in recentFiles }.forEach(File::delete)
            recentFiles.toTypedArray()
        } else {
            files
        }
    }

    /**
     * Adding 15 minutes as the backend will update the timestamp if the clock-skew is more than 30 minutes
     */
    private fun removeOldFiles(files: Array<File>, minutes: Int = 15): Array<File> {
        val cutoffTime = System.currentTimeMillis() - minutes * 60 * 1000
        return files.filter {
            if (it.lastModified() < cutoffTime) {
                val isDeleted = it.delete()
                Logger.i(if (isDeleted) "Deleted file: ${it.name}" else "Failed to delete file: ${it.name}")
                false
            } else {
                true
            }
        }.toTypedArray()
    }



}
