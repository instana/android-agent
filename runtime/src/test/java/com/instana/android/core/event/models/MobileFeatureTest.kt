/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.core.event.models

import org.junit.Assert
import org.junit.Test

class MobileFeatureTest {

    @Test
    fun `test use Feature params values`(){
        Assert.assertEquals("anr",MobileFeature.ANR.internalType)
        Assert.assertEquals("c",MobileFeature.CRASH.internalType)
        Assert.assertEquals("sn",MobileFeature.AUTO_CAPTURE_SCREEN_NAME.internalType)
        Assert.assertEquals("lm",MobileFeature.LOW_MEMORY.internalType)
        Assert.assertEquals("db",MobileFeature.DROP_BEACON.internalType)
    }
}