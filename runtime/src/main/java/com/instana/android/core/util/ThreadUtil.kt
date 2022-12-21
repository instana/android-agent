/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import androidx.annotation.RestrictTo
import java.io.PrintWriter
import java.util.*

@RestrictTo(RestrictTo.Scope.LIBRARY)
object ThreadUtil {

    fun getStackTracesFor(threadList: Array<Thread?>): HashMap<String, String> {
        val traces = hashMapOf<String, String>()
        for (thread in threadList) {
            thread?.let { notNull ->
                val trace = notNull.stackTrace.map { it.toString() }.toString()
                traces.put(notNull.name, trace)
            }
        }
        return traces
    }

    fun getAppThreads(): Array<Thread?> {
        val rootGroup = Thread.currentThread().threadGroup ?: return emptyArray()

        var threadList = arrayOfNulls<Thread>(rootGroup.activeCount())
        while (rootGroup.enumerate(threadList, false) == threadList.size) {
            threadList = arrayOfNulls(threadList.size * 2)
        }
        return threadList
    }

    fun println(out: PrintWriter, thread: Thread, stackTrace: Array<StackTraceElement>) {
        out.print("\"")
        out.print(thread.name)
        out.print("\" #")
        out.print(thread.id)
        if (thread.isDaemon) {
            out.print(" daemon")
        }
        out.print(" prio=")
        out.print(thread.priority)
        out.print(thread.state.name)
        out.println()

        for (st in stackTrace) {
            out.print("    ")
            out.print(st.className)
            out.print(".")
            out.print(st.methodName)
            out.print(" (")
            out.print(st.fileName)
            if (st.lineNumber > 0) {
                out.print(":")
                out.print(st.lineNumber)
            }
            if (st.isNativeMethod) {
                out.print("/Native Method")
            }
            out.println(")")
        }
        out.println()
    }
}