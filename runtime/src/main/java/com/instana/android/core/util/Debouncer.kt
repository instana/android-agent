package com.instana.android.core.util

import java.util.*

internal class Debouncer(
    private val millis: Long
) {
    private val timer = Timer()
    private var task: TimerTask? = null

    fun enqueue(action: () -> Unit) {
        task?.cancel()
        task = object : TimerTask() {
            override fun run() = action()
        }
        timer.schedule(task, millis)
    }
}
