package com.instana.android.core

import com.instana.android.BaseTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test

class IdProviderShould : BaseTest() {

    @Test
    fun checkIfSessionIdInitialized() {
        assertThat(IdProvider.sessionId, notNullValue())
    }

    @Test
    fun checkIsEventIdRandom() {
        val eventID = IdProvider.eventId()
        assertThat(IdProvider.eventId(), not(eventID))
    }

    @Test
    fun initAtStartUp() {
        IdProvider.init(app)
        assertThat(IdProvider.sessionId, not(isEmptyString()))
    }
}