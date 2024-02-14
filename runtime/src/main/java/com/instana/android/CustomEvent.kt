/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android

import androidx.annotation.Size
import java.util.concurrent.TimeUnit

class CustomEvent(
    /**
     * name for the event
     */
    @Size(max = 256) val eventName: String
) {

    /**
     * Timestamp in which the event started, defined in milliseconds since Epoch. Will default to Now()-duration
     */
    var startTime: Long? = null

    /**
     * Duration duration of the event defined in milliseconds. Will default to 0
     */
    var duration: Long? = null

    fun setDuration(duration: Long, timeUnit: TimeUnit) {
        this.duration = timeUnit.toMillis(duration)
    }

    /**
     * Logical view in which the event happened. Will default to the current view set in Instana.view
     */
    @Size(max = 256)
    var viewName: String? = null

    /**
     * Map of meta values. These will be merged with the global Instana.meta tags for this event; they won't be applied to any other future event
     *
     * Max Key Length: 98 characters
     *
     * Max Value Length: 1024 characters
     */
    var meta: Map<String, String>? = null

    /**
     * Tracing ID sent by the Instana-enabled server in the Server-Timing header as `intid;desc=backendTracingID`
     */
    @Size(max = 128)
    var backendTracingID: String? = null

    /**
     * Error Throwable to provide additional context, if any
     */
    var error: Throwable? = null

    /**
     * Can be used to provide any custom metrics to backend
     */
    var customMetric:Double? = null
}
