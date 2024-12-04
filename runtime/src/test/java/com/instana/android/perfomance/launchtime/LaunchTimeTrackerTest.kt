/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.launchtime

import com.instana.android.BaseTest
import com.instana.android.performance.launchtime.LaunchTimeTracker
import com.instana.android.performance.launchtime.LaunchTypeEnum
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LaunchTimeTrackerTest: BaseTest() {

    @Before
    fun reset(){
        LaunchTimeTracker.applicationStartedFromBackground = false
        setPrivateField(LaunchTimeTracker,"doneTracking",false)
        setPrivateField(LaunchTimeTracker,"initialTimeInElapsedRealtime",0L)
    }
    @Test
    fun `test startTimer should start and set initialTimeInElapsedRealtime to some value`(){
        val doneTracking = getPrivateFieldValue(LaunchTimeTracker,"doneTracking") as Boolean
        Assert.assertEquals(false,doneTracking)
        LaunchTimeTracker.startTimer()
        Thread.sleep(100)
        LaunchTimeTracker.stopTimer(LaunchTypeEnum.COLD_START)
        Thread.sleep(100)
        val doneTrackingAfter = getPrivateFieldValue(LaunchTimeTracker,"doneTracking") as Boolean
        Assert.assertEquals(true,doneTrackingAfter)
    }

    @Test
    fun `test startTimer should not work if already doneTracking`(){
        LaunchTimeTracker.startTimer()
        Thread.sleep(100)
        val initialTimeInElapsedRealtime = getPrivateFieldValue(LaunchTimeTracker,"initialTimeInElapsedRealtime") as Long
        LaunchTimeTracker.stopTimer(LaunchTypeEnum.COLD_START)
        Thread.sleep(100)
        val doneTrackingAfter = getPrivateFieldValue(LaunchTimeTracker,"doneTracking") as Boolean
        Assert.assertEquals(true,doneTrackingAfter)
        LaunchTimeTracker.startTimer()
        val initialTimeInElapsedRealtimeAfter = getPrivateFieldValue(LaunchTimeTracker,"initialTimeInElapsedRealtime") as Long
        Assert.assertEquals(initialTimeInElapsedRealtime,initialTimeInElapsedRealtimeAfter)
    }

    @Test
    fun `test stopTimer should not work if already doneTracking`(){
        LaunchTimeTracker.startTimer()
        Thread.sleep(100)
        LaunchTimeTracker.stopTimer(LaunchTypeEnum.COLD_START)
        Thread.sleep(100)
        val launchTimeInNanos = getPrivateFieldValue(LaunchTimeTracker,"launchTimeInNanos") as Long
        val doneTrackingAfter = getPrivateFieldValue(LaunchTimeTracker,"doneTracking") as Boolean
        Assert.assertEquals(true,doneTrackingAfter)
        LaunchTimeTracker.stopTimer(LaunchTypeEnum.COLD_START)
        val launchTimeInNanosAfter = getPrivateFieldValue(LaunchTimeTracker,"launchTimeInNanos") as Long
        Assert.assertEquals(launchTimeInNanos,launchTimeInNanosAfter)
    }


    @Test
    fun `test stopTimer should not work if initialTimeInElapsedRealtime is 0L`(){
        val launchTimeInNanos = getPrivateFieldValue(LaunchTimeTracker,"launchTimeInNanos") as Long
        LaunchTimeTracker.stopTimer(LaunchTypeEnum.COLD_START)
        Thread.sleep(100)
        val launchTimeInNanosAfter = getPrivateFieldValue(LaunchTimeTracker,"launchTimeInNanos") as Long
        Assert.assertEquals(launchTimeInNanos,launchTimeInNanosAfter)
    }
}