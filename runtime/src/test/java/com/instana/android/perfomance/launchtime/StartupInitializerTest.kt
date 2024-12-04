/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.launchtime

import android.app.ActivityManager
import android.content.Context
import com.instana.android.performance.launchtime.StartupInitializer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations

class StartupInitializerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var activityManager: ActivityManager

    @Mock
    lateinit var runningAppProcessInfo: ActivityManager.RunningAppProcessInfo

    @Mock
    private lateinit var startupInitializer: StartupInitializer

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        startupInitializer = Mockito.spy(StartupInitializer())
        Mockito.`when`(startupInitializer.context).thenReturn(context)
        runningAppProcessInfo = Mockito.mock(ActivityManager.RunningAppProcessInfo::class.java)
    }

    @Test
    fun `onCreate returns false when app is not in foreground`() {
        // Arrange
        Mockito.`when`(context.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(activityManager)
        Mockito.`when`(activityManager.runningAppProcesses).thenReturn(
            listOf(runningAppProcessInfo)
        )
        // Act
        val result = startupInitializer.onCreate()

        // Assert
        Assert.assertFalse(result)
        Mockito.verify(startupInitializer,times(2)).context // Verify context was accessed
    }

    @Test
    fun `onCreate returns false when context is null`() {
        // Arrange
        Mockito.`when`(startupInitializer.context).thenReturn(null)

        // Act
        val result = startupInitializer.onCreate()

        // Assert
        Assert.assertFalse(result)
    }
}
