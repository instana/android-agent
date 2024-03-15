/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import com.instana.android.BaseTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class ThreadUtilTest:BaseTest() {
    
    
    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
    }
    
    @Test
    fun `test getAppThreads function return non null value`(){
        Assert.assertNotNull(ThreadUtil.getAppThreads())
    }
    
    @Test
    fun `test getStackTracesFor thread list should return traces`(){
        val listOfThreads = arrayOf<Thread?>(Thread())
        val mappedItem = ThreadUtil.getStackTracesFor(listOfThreads)
        assert(mappedItem.size>0)
    }
}