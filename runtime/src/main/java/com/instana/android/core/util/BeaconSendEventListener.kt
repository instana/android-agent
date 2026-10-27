/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.util

import okhttp3.Call
import okhttp3.EventListener
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * OkHttp EventListener that tracks whether the full request body was successfully written
 * to the OS socket buffer before any IOException occurs.
 *
 * One instance must be created per [send] invocation and attached via
 * [okhttp3.OkHttpClient.newBuilder().eventListener()][okhttp3.OkHttpClient.Builder.eventListener].
 * This gives it exactly the same lifetime as the OkHttp call — no shared state, no risk
 * of a stale [requestBodySent] value from a previous call bleeding into the current one.
 *
 * The previous implementation used a shared [EventListener.Factory] with an
 * [AtomicReference] to the last-created listener. Under the following sequence that
 * approach produced a false-positive `bodyFullySent=true` on a ConnectException:
 *   1. Worker A succeeds → listener A: requestBodySent=true stored in factory.lastListener
 *   2. Worker B (new process/reconnect) fails with ConnectException
 *      → factory.lastListener still holds listener A (requestBodySent=true)
 *      → bodySent=true incorrectly → staging files deleted → beacons lost
 *      → next queue() writes the same beacon again → 3 copies sent to Kafka
 *
 * If [requestBodySent] is true when [callFailed] fires, the server has already received the
 * complete beacon payload. Retrying would send duplicates; staging files must be deleted.
 *
 * If [requestBodySent] is false (DNS failure, connect timeout, mid-body write timeout),
 * the server has not received the data and normal retry logic applies.
 */
internal class BeaconSendEventListener : EventListener() {

    /** True once requestBodyEnd fires — the full body reached the OS socket TX buffer. */
    val requestBodySent = AtomicBoolean(false)

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        requestBodySent.set(true)
    }

    override fun callFailed(call: Call, ioe: IOException) {
        Logger.i("OkHttp callFailed, bodyFullySent=${requestBodySent.get()}, error=${ioe.message}")
    }
}
