/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android

interface Logger {
    /**
     *
     * @param level one of android.util.Log levels (android.util.Log.INFO, android.util.Log.ERROR, ...)
     * @param tag internal Instana Agent tag
     * @param message log message
     * @param error error throwable, if any
     */
    fun log(level: Int, tag: String, message: String, error: Throwable?)
}
