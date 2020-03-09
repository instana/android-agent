package com.instana.android.crash

import android.app.Application
import android.util.Log
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.EventFactory
import com.instana.android.core.util.ConstantsAndUtil.getAppVersionNameAndVersionCode
import java.lang.Thread.currentThread
import java.util.*
import java.util.concurrent.LinkedBlockingDeque

/**
 * Handling crash related actions
 */
class CrashService(
    private val app: Application,
    private val manager: InstanaWorkManager,
    private val config: InstanaConfig,
    defaultThreadHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
) {

    private var breadCrumbs: Queue<String> = LinkedBlockingDeque()
    private var handler: ExceptionHandler? = null

    init {
        if (defaultThreadHandler != null) {
            handler = ExceptionHandler(this, defaultThreadHandler)
        }
        if (config.enableCrashReporting) {
            handler?.enable()
        } else {
            handler?.disable()
        }
    }

    fun changeBufferSize(size: Int) {
        // TODO should this not just remove the first X elements (if necessary)?
        config.breadcrumbsBufferSize = size
        breadCrumbs.clear()
    }

    fun leave(breadCrumb: String) {
        breadCrumbs.add(breadCrumb)
        if (breadCrumbs.size > config.breadcrumbsBufferSize) {
            breadCrumbs.poll()
        }
    }

    fun submitCrash(thread: Thread?, throwable: Throwable?) {
        val breadCrumbsCopy = breadCrumbs.toList()
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

        manager.persistCrash(
            EventFactory.createCrash(
                version, versionCode,
                breadCrumbsCopy, stackTrace, appStackTraces
            )
        )
        breadCrumbs.clear()
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
        val rootGroup = currentThread().threadGroup ?: return emptyArray()

        var threadList = arrayOfNulls<Thread>(rootGroup.activeCount())
        while (rootGroup.enumerate(threadList, false) == threadList.size) {
            threadList = arrayOfNulls(threadList.size * 2)
        }
        return threadList
    }
}