/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance.mem

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
import android.content.Context
import android.content.res.Configuration
import com.instana.android.Instana
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import com.instana.android.performance.PerformanceMetric
import com.instana.android.performance.PerformanceMonitor
import kotlin.properties.Delegates

class LowMemoryMonitor(
    private val app: Application,
    private val lifeCycle: InstanaLifeCycle,
) : ComponentCallbacks2, PerformanceMonitor {

    override var enabled by Delegates.observable(false) { _, oldValue, newValue ->
        when {
            oldValue == newValue -> Unit
            newValue -> app.registerComponentCallbacks(this)
            newValue.not() -> app.unregisterComponentCallbacks(this)
        }
        Logger.i("LowMemoryMonitor enabled: $newValue")
    }

    override fun onLowMemory() {
        Logger.i("On Low Memory called!")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // implement if needed info on configuration change
    }

    override fun onTrimMemory(level: Int) {
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            val activityName = lifeCycle.activityName ?: ""
            sendLowMemoryEvent(activityName)

        }
    }

    private fun sendLowMemoryEvent(activityName: String) {
        Logger.d("LowMemory detected with: `activityName` $activityName")
        //Collecting Heap Memory Details
        val maxMem = ConstantsAndUtil.runtime.maxMemory()
        val usedMem = ConstantsAndUtil.runtime.totalMemory() - ConstantsAndUtil.runtime.freeMemory()
        val availableMem = maxMem - usedMem
        val maxInMb = maxMem / MB
        val availableInMb = availableMem / MB
        val usedInMb = usedMem / MB
        Logger.i("Heap Memory - Maximum: $maxMem , Used: $usedMem , Available: $availableMem ")
        Instana.performanceReporterService?.sendPerformance(PerformanceMetric.OutOfMemory(
            availableMb = availableInMb,
            usedMb = usedInMb,
            maximumMb = maxInMb)
        )
        // Collecting Device RAM memory details
        val activityManager = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        // Memory details
        val totalMemory = memoryInfo.totalMem // Total system memory in bytes
        val availableMemory = memoryInfo.availMem // Available system memory in bytes
        val isLowMemory = memoryInfo.lowMemory // Is the system in low-memory state?

        val usedMemory = totalMemory - availableMemory
        val totalMemoryInMB = totalMemory / MB
        val availableMemoryInMB = availableMemory / MB
        val usedMemoryInMB = usedMemory / MB

        // Log the memory info
        Logger.i("RAM Memory: is Low Memory: $isLowMemory | Total: $totalMemoryInMB Mb | Available: $availableMemoryInMB Mb | Used: $usedMemoryInMB Mb")
    }

    companion object {
        const val MB = (1024L * 1024L)
    }
}
