/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance.frame

import android.os.SystemClock
import android.view.Choreographer
import com.instana.android.Instana
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.Logger
import com.instana.android.performance.PerformanceMonitor
import com.instana.android.performance.PerformanceMonitorConfig
import kotlin.properties.Delegates

class FrameSkipMonitor(
    private val performanceMonitorConfig: PerformanceMonitorConfig,
    private val lifeCycle: InstanaLifeCycle,
    private val choreographer: Choreographer = Choreographer.getInstance()
) : Choreographer.FrameCallback, PerformanceMonitor {

    override var enabled by Delegates.observable(false) { _, oldValue, newValue ->
        when {
            oldValue == newValue -> Unit
            newValue -> choreographer.postFrameCallbackDelayed(
                this,
                START_DELAY_TIME_MS
            )
            newValue.not() -> choreographer.removeFrameCallback(this)
        }
        Logger.i("FrameSkipMonitor enabled: $newValue")
    }

    var appInBackground by Delegates.observable(false) { _, oldValue, newValue ->
        when {
            oldValue == newValue -> Unit
            enabled.not() -> Unit
            newValue.not() -> choreographer.postFrameCallbackDelayed(
                this,
                START_DELAY_TIME_MS
            )
            newValue -> choreographer.removeFrameCallback(this)
        }
    }

    private var dipActive: Boolean = false

    private var lastFrameTime: Long? = null
    private var startTime: Long = 0L
    private var duration: Long = 0L

    private var frames = mutableListOf<Long>()

    override fun doFrame(frameTimeNanos: Long) {
        lastFrameTime = if (lastFrameTime != null) {
            val frameRate = calculateFPS()
            if (frameRate.toInt() < performanceMonitorConfig.frameRateDipThreshold) {
                frames.add(frameRate)
                if (!dipActive) {
                    startTime = System.currentTimeMillis()
                    dipActive = true
                }
            } else {
                if (dipActive) {
                    duration = System.currentTimeMillis() - startTime
                    dipActive = false
                    checkConditionsAndSendEvent()
                    frames.clear()
                }
            }
            SystemClock.elapsedRealtime()
        } else {
            SystemClock.elapsedRealtime()
        }

        if (enabled) {
            choreographer.postFrameCallback(this)
        }
    }

    private fun checkConditionsAndSendEvent() {
        val averageFrameRate = frames.average().toLong()
        if (averageFrameRate != 0L) {
            sendFrameDipEvent(averageFrameRate)
        }
    }

    private fun sendFrameDipEvent(averageFrameRate: Long) {
        val activityName = lifeCycle.activityName ?: EMPTY_STR
        Logger.d("FrameDip detected with: `activityName` $activityName, `avgFrameRate` $averageFrameRate")
        Instana.customEvents?.submit(
            eventName = "FrameDip",
            startTime = startTime,
            duration = duration,
            meta = mapOf(
                "activityName" to activityName,
                "avgFrameRate" to averageFrameRate.toString()
            ),
            viewName = Instana.view,
            backendTracingID = null,
            error = null,
            customMetric = null
        )
    }

    private fun calculateFPS(): Long {
        val elapsedTime = SystemClock.elapsedRealtime() - lastFrameTime!!
        return if (elapsedTime == 0L) {
            performanceMonitorConfig.frameRateDipThreshold.toLong()
        } else {
            (1000 / elapsedTime)
        }
    }

    companion object {
        // during app start add delay to monitoring while app settles
        private const val START_DELAY_TIME_MS = 4000L
    }
}