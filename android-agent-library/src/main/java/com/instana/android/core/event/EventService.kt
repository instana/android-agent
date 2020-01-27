package com.instana.android.core.event

import com.instana.android.core.InstanaMonitor
import com.instana.android.core.InstanaWorkManager

class EventService(
        private val manager: InstanaWorkManager
) : InstanaMonitor {

    private var enabled: Boolean = true

    override fun enable() {
        enabled = true
    }

    override fun disable() {
        enabled = false
    }

    fun submit(event: BaseEvent) {
        if (enabled) {
            manager.send(event)
        }
    }

    fun submit(name: String, type: String, startTime: Long, duration: Long) {
        val event = EventFactory.createCustom(mapOf(name to type), startTime, duration)
        submit(event)
    }
}
