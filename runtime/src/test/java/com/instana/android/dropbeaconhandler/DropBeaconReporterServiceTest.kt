/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.dropbeaconhandler

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class DropBeaconReporterServiceTest:BaseTest() {

    private lateinit var dropBeaconReporterService: DropBeaconReporterService

    @Before
    fun setup(){
        dropBeaconReporterService = DropBeaconReporterService(app,mockWorkManager,config = InstanaConfig(
            InstanaTest.API_KEY,
            InstanaTest.SERVER_URL, enableCrashReporting = true)
        )
    }
    @Test
    fun `test sendDrop function should send a drop beacon`(){
        Instana.sessionId = "1223"
        dropBeaconReporterService.sendDrop(
            internalMetaInfo = mapOf("drop" to "drop"), droppingStartTime = 5629, dropBeaconStartView = null
        )
        Mockito.verify(mockWorkManager).queue(any())
    }

    @Test
    fun `test sendDrop function should not send a drop beacon if session is null`(){
        Instana.sessionId = null
        dropBeaconReporterService.sendDrop(
            internalMetaInfo = mapOf("drop" to "drop"), droppingStartTime = 5629, dropBeaconStartView = null
        )
        Mockito.verify(mockWorkManager, never()).queue(any())
    }


}