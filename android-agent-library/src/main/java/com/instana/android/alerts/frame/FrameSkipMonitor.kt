package com.instana.android.alerts.frame

import android.os.SystemClock
import android.view.Choreographer
import androidx.annotation.VisibleForTesting
import com.instana.android.Instana
import com.instana.android.alerts.AlertsConfiguration
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaMonitor
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR

class FrameSkipMonitor(
    private val alertsConfiguration: AlertsConfiguration,
    private val lifeCycle: InstanaLifeCycle,
    private val choreographer: Choreographer = Choreographer.getInstance()
) : Choreographer.FrameCallback, InstanaMonitor {

    private var enabled: Boolean = alertsConfiguration.reportingEnabled
    private var dipActive: Boolean = false

    private var lastFrameTime: Long? = null
    private var startTime: Long = 0L
    private var duration: Long = 0L

    private var frames = mutableListOf<Long>()

    init {
        if (enabled) {
            choreographer.postFrameCallbackDelayed(this, START_DELAY_TIME_MS)
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        lastFrameTime = if (lastFrameTime != null) {
            val frameRate = calculateFPS()
            if (frameRate.toInt() < alertsConfiguration.frameRateDipThreshold) {
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
        Instana.customEvents?.submit(
            name = "FrameDip",
            startTime = startTime,
            duration = duration,
            meta = mapOf(
                "activityName" to activityName,
                "avgFrameRate" to averageFrameRate.toString()
            )
        )
    }

    private fun calculateFPS(): Long {
        val elapsedTime = SystemClock.elapsedRealtime() - lastFrameTime!!
        return if (elapsedTime == 0L) {
            alertsConfiguration.frameRateDipThreshold.toLong()
        } else {
            (1000 / elapsedTime)
        }
    }

    override fun enable() {
        if (!enabled) {
            choreographer.postFrameCallbackDelayed(this, START_DELAY_TIME_MS)
        }
        enabled = true
    }

    @VisibleForTesting
    fun enableWithNoDelay() {
        enabled = true
        choreographer.postFrameCallback(this)
    }

    override fun disable() {
        if (enabled) {
            choreographer.removeFrameCallback(this)
        }
        enabled = false
    }

    companion object {
        // during app start add delay to monitoring while app settles
        private const val START_DELAY_TIME_MS = 4000L
    }
}