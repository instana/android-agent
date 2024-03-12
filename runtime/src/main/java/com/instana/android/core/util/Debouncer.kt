/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import java.util.Timer
import java.util.TimerTask

internal class Debouncer {
    private var timer: Timer? = null

    fun enqueue(millis: Long, action: () -> Unit) {
        timer?.cancel()

        val task = object : TimerTask() {
            override fun run() = action()
        }
        timer = Timer("Debouncer").apply {
            schedule(task, millis)
        }
    }
}
