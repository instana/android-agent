/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.instana.android.BaseTest
import com.instana.android.view.VisibleScreenNameTracker
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class InstanaActivityLifecycleCallbacksTest:BaseTest() {

    @Mock
    private lateinit var mockActivity: FragmentActivity

    private lateinit var lifecycleCallbacks: InstanaActivityLifecycleCallbacks

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        lifecycleCallbacks = spy(InstanaActivityLifecycleCallbacks())
    }

    @Test
    fun `onActivityResumed should log and update screen tracker`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        lifecycleCallbacks.onActivityResumed(mockActivity)
        verify(lifecycleCallbacks, times(1)).onActivityResumed(mockActivity)
    }

    @Test
    fun `onActivityPaused should log and update screen tracker1`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        lifecycleCallbacks.onActivityPaused(mockActivity)
        verify(lifecycleCallbacks, times(1)).onActivityPaused(mockActivity)
    }

    @Test
    fun `onActivityDestroyed should log and update screen tracker1`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        lifecycleCallbacks.onActivityDestroyed(mockActivity)
        verify(lifecycleCallbacks, times(1)).onActivityDestroyed(mockActivity)
    }

    @Test
    fun `onActivityStopped should log and update screen tracker1`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        lifecycleCallbacks.onActivityStopped(mockActivity)
        verify(lifecycleCallbacks, times(1)).onActivityStopped(mockActivity)
    }

    @Test
    fun `onActivityStarted should log and update screen tracker1`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        lifecycleCallbacks.onActivityStarted(mockActivity)
        verify(lifecycleCallbacks, times(1)).onActivityStarted(mockActivity)
    }

    @Test
    fun `onActivityCreated should log and update screen tracker1`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        val mockBundle = mock(Bundle::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        lifecycleCallbacks.onActivityCreated(mockActivity,mockBundle)
        verify(lifecycleCallbacks, times(1)).onActivityCreated(mockActivity,mockBundle)
    }

    @Test
    fun `onActivitySaveInstanceState should log and update screen tracker1`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        val mockBundle = mock(Bundle::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        lifecycleCallbacks.onActivitySaveInstanceState(mockActivity,mockBundle)
        verify(lifecycleCallbacks, times(1)).onActivitySaveInstanceState(mockActivity,mockBundle)
    }

    @Test
    fun `updateScreenTracker should update VisibleScreenNameTracker`() {
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        invokePrivateMethod2(lifecycleCallbacks,"updateScreenTracker",mockActivity, Activity::class.java)
        assert(VisibleScreenNameTracker.initialViewMap.isNotEmpty())
    }

    @Test
    fun `updateScreenTracker should handle null content description`() {
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findContentDescription()).thenReturn(null)
        invokePrivateMethod2(lifecycleCallbacks,"updateScreenTracker",mockActivity, Activity::class.java)
        assert(VisibleScreenNameTracker.initialViewMap.isNotEmpty())
    }
}
