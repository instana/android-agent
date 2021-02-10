/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance.anr

/**
 * A [Runnable] which calls [.notifyAll] when run.
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class AnrSupervisorCallback : Runnable {

    /**
     * Flag storing whether [.run] was called
     */
    /**
     * Returns whether [.run] was called yet
     *
     * @return true if called, false if not
     */
    @get:Synchronized
    internal var isCalled: Boolean = false
        private set

    @Synchronized
    override fun run() {
        this.isCalled = true
        (this as java.lang.Object).notifyAll()
    }
}