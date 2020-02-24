package com.instana.android.core.event

import com.instana.android.BaseTest
import com.instana.android.core.InstanaWorkManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class CustomEventServiceShould : BaseTest() {

    private val managerMock: InstanaWorkManager = mock()
    private val eventService = CustomEventService(managerMock)

    @Test
    fun submitEvent() {
        val event = EventFactory.createCustom(mapOf("name" to "type"), 0L, 0L)
        eventService.submit(event)
        verify(managerMock).send(event)
    }

    @Test
    fun submitEventWithParameters() {
        eventService.submit("name", "type", 0L, 0L)
        verify(managerMock).send(any())
    }
}