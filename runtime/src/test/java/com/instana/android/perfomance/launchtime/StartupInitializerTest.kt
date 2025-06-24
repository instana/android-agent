/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.launchtime

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.instana.android.BaseTest
import com.instana.android.performance.launchtime.StartupInitializer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StartupInitializerTest :BaseTest(){

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

    @Test
    fun `test query returns null with startupInitializer class`() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)  // Mocking context properly
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)
        val mockContentResolver = Mockito.mock(ContentResolver::class.java) // Mock content resolver

        // Set the mocked content resolver to the mocked context
        Mockito.`when`(mockContext.contentResolver).thenReturn(mockContentResolver)

        // If the query method uses the context, set it properly in startupInitializer.
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)

        // Act
        val result = startupInitializer.query(
            uri = Uri.parse("http://www.google.com"),
            projection = null,
            selection = null,
            selectionArgs = null,
            sortOrder = null
        )
        // Assert
        Assert.assertEquals(result,null)
    }

    @Test
    fun `test getType returns null with startupInitializer class`() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)  // Mocking context properly
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)
        val mockContentResolver = Mockito.mock(ContentResolver::class.java) // Mock content resolver

        // Set the mocked content resolver to the mocked context
        Mockito.`when`(mockContext.contentResolver).thenReturn(mockContentResolver)

        // If the query method uses the context, set it properly in startupInitializer.
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)

        // Act
        val result = startupInitializer.getType(
            uri = Uri.parse("http://www.google.com")
        )
        // Assert
        Assert.assertEquals(result,null)
    }

    @Test
    fun `test insert returns null with startupInitializer class`() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)  // Mocking context properly
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)
        val mockContentResolver = Mockito.mock(ContentResolver::class.java) // Mock content resolver

        // Set the mocked content resolver to the mocked context
        Mockito.`when`(mockContext.contentResolver).thenReturn(mockContentResolver)

        // If the query method uses the context, set it properly in startupInitializer.
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)

        // Act
        val result = startupInitializer.insert(
            uri = Uri.parse("http://www.google.com"), values = null,
        )
        // Assert
        Assert.assertEquals(result,null)
    }

    @Test
    fun `test delete returns null with startupInitializer class`() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)  // Mocking context properly
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)
        val mockContentResolver = Mockito.mock(ContentResolver::class.java) // Mock content resolver

        // Set the mocked content resolver to the mocked context
        Mockito.`when`(mockContext.contentResolver).thenReturn(mockContentResolver)

        // If the query method uses the context, set it properly in startupInitializer.
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)

        // Act
        val result = startupInitializer.delete(
            uri = Uri.parse("http://www.google.com"), selection = null, selectionArgs = null,
        )
        // Assert
        Assert.assertEquals(result,0)
    }

    @Test
    fun `test update returns null with startupInitializer class`() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)  // Mocking context properly
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)
        val mockContentResolver = Mockito.mock(ContentResolver::class.java) // Mock content resolver

        // Set the mocked content resolver to the mocked context
        Mockito.`when`(mockContext.contentResolver).thenReturn(mockContentResolver)

        // If the query method uses the context, set it properly in startupInitializer.
        Mockito.`when`(startupInitializer.context).thenReturn(mockContext)

        // Act
        val result = startupInitializer.update(uri =Uri.parse("http://www.google.com"), values = null, selection = "invidunt", selectionArgs = arrayOf())
        // Assert
        Assert.assertEquals(result,0)
    }

}
