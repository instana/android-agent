/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.only
import org.mockito.MockitoAnnotations

class DefaultActivityLifecycleCallbacksTest {
    @Mock
    lateinit var implementation:DefaultActivityLifecycleCallbacks
    @Before
    fun `test setup`(){
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test onActivityCreated should be called only once`() {
        val mockActivity = mock(Activity::class.java)
        val mockBundle = mock(Bundle::class.java)
        val mockPersistableBundle = mock(PersistableBundle::class.java)
        mockActivity.onCreate(mockBundle,mockPersistableBundle)
        implementation.onActivityCreated(mockActivity, mockBundle)
        implementation.onActivityResumed(mockActivity)
        implementation.onActivityCreated(mockActivity,mockBundle)
        implementation.onActivityPaused(mockActivity)
        implementation.onActivityStopped(mockActivity)
        implementation.onActivityPreCreated(mockActivity,mockBundle)
        implementation.onActivityDestroyed(mockActivity)
        implementation.onActivitySaveInstanceState(mockActivity,mockBundle)
        verify(mockActivity, only()).onCreate(any(), any())
    }
    
}