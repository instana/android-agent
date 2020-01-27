package com.instana.android.crash

import android.app.Application
import android.util.Log
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.InstanaMonitor
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.EventFactory
import com.instana.android.core.util.ConstantsAndUtil.getAppVersionNameAndVersionCode
import org.apache.commons.collections4.QueueUtils
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.lang.Thread.currentThread
import java.util.*

/**
 * Handling crash related actions
 */
class CrashService(
        private val app: Application,
        private val manager: InstanaWorkManager,
        private val configuration: InstanaConfiguration,
        defaultThreadHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
) : InstanaMonitor {

    private var breadCrumbs: Queue<String>? = null
    private var handler: ExceptionHandler? = null

    init {
        breadCrumbs = QueueUtils.synchronizedQueue(CircularFifoQueue(configuration.breadcrumbsBufferSize))
        if (defaultThreadHandler != null) {
            handler = ExceptionHandler(this, defaultThreadHandler)
        }
        if (configuration.enableCrashReporting) {
            handler?.enable()
        } else {
            handler?.disable()
        }
    }

    override fun enable() {
        configuration.enableCrashReporting = true
        handler?.enable()
    }

    override fun disable() {
        configuration.enableCrashReporting = false
        handler?.disable()
        breadCrumbs?.clear()
    }

    fun changeBufferSize(size: Int) {
        configuration.breadcrumbsBufferSize = size
        breadCrumbs?.clear()
        breadCrumbs = null
        breadCrumbs = QueueUtils.synchronizedQueue(CircularFifoQueue(size))
    }

    fun leave(breadCrumb: String) {
        breadCrumbs?.add(breadCrumb)
    }

    fun submitCrash(thread: Thread?, throwable: Throwable?) {
        val breadCrumbList = breadCrumbs?.toList()
        val stackTrace = Log.getStackTraceString(throwable)

        val stackTraces = Thread.getAllStackTraces()

        if (!stackTraces.containsKey(thread)) {
            stackTraces[thread] = thread?.stackTrace
        }

        if (throwable != null) { // unhandled errors use the exception trace
            stackTraces[thread] = throwable.stackTrace
        }

        val threadList = getAppThreads()
        val appStackTraces = getStackTracesFor(threadList)

        val (versionCode: String, version: String) = getAppVersionNameAndVersionCode(app)

        manager.persistCrash(EventFactory.createCrash(version, versionCode,
                breadCrumbList ?: emptyList(), stackTrace, appStackTraces))
        breadCrumbs?.clear()
    }

    private fun getStackTracesFor(threadList: Array<Thread?>): HashMap<String, String> {
        val traces = hashMapOf<String, String>()
        threadList.forEach { thread ->
            thread?.let { notNull ->
                val trace = notNull.stackTrace.map { it.toString() }.toString()
                traces.put(notNull.name, trace)
            }
        }
        return traces
    }

    private fun getAppThreads(): Array<Thread?> {
        val rootGroup = currentThread().threadGroup

        var threadList = arrayOfNulls<Thread>(rootGroup.activeCount())
        while (rootGroup.enumerate(threadList, false) == threadList.size) {
            threadList = arrayOfNulls(threadList.size * 2)
        }
        return threadList
    }
}