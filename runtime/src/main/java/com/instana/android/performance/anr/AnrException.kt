/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance.anr

import com.instana.android.core.util.Logger
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Locale

/**
 * A [Exception] to represent an ANR. This [Exception]'s
 * stack trace will be the current stack trace of the given
 * [Thread]
 */
class AnrException
/**
 * Creates a new instance
 *
 * @param thread the [Thread] which is not responding
 */
    (thread: Thread) : Exception("ANR detected") {

    init {
        // Copy the Thread's stack,
        // so the Exception seams to occure there
        this.stackTrace = thread.stackTrace
    }

    /**
     * Logs the current process and all its threads
     */
    fun logProcessMap() {
        val bos = ByteArrayOutputStream()
        val ps = PrintStream(bos)
        this.printProcessMap(ps)
        Logger.i(String(bos.toByteArray()))
    }

    /**
     * Prints the current process and all its threads
     *
     * @param ps the [PrintStream] to which the
     * info is written
     */
    private fun printProcessMap(ps: PrintStream) {
        // Get all stack traces in the system
        val stackTraces = Thread.getAllStackTraces()

        ps.println("Process map:")

        for (thread in stackTraces.keys) {
            if (stackTraces[thread]?.isNotEmpty() == true) {
                this.printThread(ps, Locale.getDefault(), thread, stackTraces[thread]!!)
                ps.println()
            }
        }
    }

    /**
     * Prints the given thread
     *
     * @param ps     the [PrintStream] to which the info is written
     * @param l      the [Locale] to use
     * @param thread the [Thread] to print
     * @param stack  the [Thread]'s stack trace
     */
    private fun printThread(ps: PrintStream, l: Locale, thread: Thread, stack: Array<StackTraceElement>) {
        ps.println(String.format(l, "\t%s (%s)", thread.name, thread.state))

        for (element in stack) {
            ps.println(
                String.format(
                    l, "\t\t%s.%s(%s:%d)",
                    element.className,
                    element.methodName,
                    element.fileName,
                    element.lineNumber
                )
            )

        }
    }
}