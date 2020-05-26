package com.instana.android

import java.util.concurrent.TimeUnit

class CustomEvent(
    /**
     * name for the event
     */
    val eventName: String
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
    var viewName: String? = null

    /**
     * Set of meta values. These will be merged with the global Instana.meta tags for this event; they won't be applied any future event
     */
    var meta: Map<String, String>? = null

    /**
     * Tracing ID sent by the Instana-enabled server in the Server-Timing header as `intid;desc=backendTracingID`
     */
    var backendTracingID: String? = null

    /**
     * Error Throwable, if any
     */
    var error: Throwable? = null
}
