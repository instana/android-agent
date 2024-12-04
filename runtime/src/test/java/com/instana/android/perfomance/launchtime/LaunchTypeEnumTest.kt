/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.launchtime

import com.instana.android.performance.launchtime.LaunchTypeEnum
import org.junit.Assert
import org.junit.Test

class LaunchTypeEnumTest {

    @Test
    fun `test coldStart text value`(){
        //Need to be updated based on the backend changes/acceptance
        Assert.assertEquals("cold_Start",LaunchTypeEnum.COLD_START.value)
    }

    @Test
    fun `test warmStart text value`(){
        //Need to be updated based on the backend changes/acceptance
        Assert.assertEquals("warm_start",LaunchTypeEnum.WARM_START.value)
    }


}