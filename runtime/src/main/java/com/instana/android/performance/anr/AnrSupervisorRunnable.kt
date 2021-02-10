/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance.anr

import android.os.Handler
import android.os.Looper
import com.instana.android.core.util.Logger
import com.instana.android.performance.PerformanceMonitorConfig
import java.util.concurrent.TimeUnit

/**
 * A [Runnable] testing the UI thread every 10s until [.stop] is called
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class AnrSupervisorRunnable
internal constructor(
    private val performanceMonitorConfig: PerformanceMonitorConfig,
    private val anrCallback: AnrSupervisor.AnrCallback
) : Runnable {

    /**
     * The [Handler] to access the UI threads message queue
     */
    private val handler = Handler(Looper.getMainLooper())

    /**
     * The stop flag
     */
    private var stopped: Boolean = false

    /**
     * Flag indicating the stop was performed
     */
    /**
     * Returns whether the stop is completed
     *
     * @return true if stop is completed, false if not
     */
    @get:Synchronized
    var isStopped = true
        private set

    private var startTime: Long? = null
    private var duration: Long? = null

    override fun run() {
        this.isStopped = false
        // Loop until stop() was called or thread is interrupted
        while (!Thread.interrupted()) {
            try {
                // Create new callback
                val callback = AnrSupervisorCallback()
                // Perform test, Handler should run
                // the callback within alertsConfiguration.anrThreshold
                synchronized(callback) {
                    this.handler.post(callback)
                    (callback as java.lang.Object).wait(performanceMonitorConfig.anrThresholdMs)

                    // Check if called
                    if (!callback.isCalled) {
                        startTime = System.currentTimeMillis()
                        // Wait until the thread responds again
                        callback.wait()
                    } else {
                        if (startTime != null) {
                            duration = System.currentTimeMillis() - startTime!!
                            Logger.i("UI Thread blocked for $duration")
                            val e = AnrException(this.handler.looper.thread)
                            anrCallback.onAppNotResponding(e, duration!!)
//                            e.logProcessMap()
                            startTime = null
                        }
                    }
                }
                // Check if stopped
                this.checkStopped()
                // Sleep for next test
                TimeUnit.SECONDS.sleep(TEST_TIMEOUT)
            } catch (e: InterruptedException) {
                Logger.e("Failed to run ANRSupervisor", e)
                break
            }
        }
        // Set stop completed flag
        this.isStopped = true
    }

    @Synchronized
    @Throws(InterruptedException::class)
    private fun checkStopped() {
        if (this.stopped) {
            // Wait 1000ms
            TimeUnit.SECONDS.sleep(1L)
            // Break if still stopped
            if (this.stopped) {
                throw InterruptedException()
            }
        }
    }

    /**
     * Stops the check
     */
    @Synchronized
    fun stop() {
        this.stopped = true
    }

    /**
     * Stops the check
     */
    @Synchronized
    fun unStop() {
        this.stopped = false
    }

    companion object {
        private const val TEST_TIMEOUT = 5L
    }
}