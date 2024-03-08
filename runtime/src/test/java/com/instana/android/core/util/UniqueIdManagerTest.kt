/*
 * IBM Confidential
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.util
import android.content.Context
import android.content.SharedPreferences
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class UniqueIdManagerTest {

    // Mocks for the Context, SharedPreferences, and SharedPreferences.Editor
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockSharedPrefsUtil: SharedPrefsUtil

    private val KEY_UNIQUE_ID = "usi_uniqueId"
    private val KEY_TIMESTAMP = "usi_timestamp"

    // Setup method to initialize Mockito mocks
    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)

        // Mock SharedPrefsUtil behavior
        `when`(mockSharedPrefsUtil.getString(mockContext, KEY_UNIQUE_ID)).thenReturn(null)
        `when`(mockSharedPrefsUtil.getLong(mockContext, KEY_TIMESTAMP)).thenReturn(0)
    }

    // Test to verify the behavior of initialize with a valid existing uniqueId
    @Test
    fun `test initialize with valid existing uniqueId`() {
        // Arrange
        val existingUniqueId = "existingUniqueId"
        val currentTime = System.currentTimeMillis()
        val validTimeThresholdInHrs = 24L
        `when`(mockSharedPreferences.getString(eq(KEY_UNIQUE_ID), any())).thenReturn(existingUniqueId)
        `when`(mockSharedPreferences.getLong(eq(KEY_TIMESTAMP), anyLong())).thenReturn(currentTime)

        // Act
        UniqueIdManager.initialize(mockContext, validTimeThresholdInHrs)

        // Assert
        verify(mockSharedPreferences, never()).edit() // Make sure sharedPreferences.edit() is not called
    }

    // Test to verify the behavior of initialize with an expired uniqueId
    @Test
    fun `test initialize with expired uniqueId`() {
        // Arrange
        val expiredUniqueId = "expiredUniqueId"
        val currentTime = System.currentTimeMillis()
        val expiredTimeThresholdInHrs = 1L
        `when`(mockSharedPreferences.getString(eq(KEY_UNIQUE_ID), any())).thenReturn(expiredUniqueId)
        `when`(mockSharedPreferences.getLong(eq(KEY_TIMESTAMP), anyLong())).thenReturn(currentTime - (expiredTimeThresholdInHrs * 3600000) - 1)

        // Act
        UniqueIdManager.initialize(mockContext, expiredTimeThresholdInHrs)

        // Assert
        verify(mockEditor, times(2)).apply()
    }

    // Test to verify the behavior of initialize with a missing uniqueId
    @Test
    fun `test initialize with missing uniqueId`() {
        // Arrange
        val validTimeThresholdInHrs = 24L
        `when`(mockSharedPreferences.getString(eq(KEY_UNIQUE_ID), any())).thenReturn(null)

        // Capture the arguments passed to putString
        val uniqueIdCaptor = ArgumentCaptor.forClass(String::class.java)

        // Act
        UniqueIdManager.initialize(mockContext, validTimeThresholdInHrs)

        // Assert
        // Verify that putString was called twice with the correct argument, and apply() was called twice each
        verify(mockEditor, times(2)).putString(eq(KEY_UNIQUE_ID), uniqueIdCaptor.capture())
        verify(mockEditor, times(2)).putLong(eq(KEY_TIMESTAMP), anyLong())
        verify(mockEditor, times(4)).apply()

        // Assert the value passed to putString
        val generatedUniqueId = uniqueIdCaptor.value
        assertNotNull(generatedUniqueId) // Ensure the generated value is not null
    }

    // Test to verify the behavior of initialize with various usiRefreshTimeIntervalInHrs values
    @Test
    fun `test initialize with different usiRefreshTimeIntervalInHrs values`() {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val expiredTimeThresholdInHrs = 1L
        val neverTrackTimeThresholdInHrs = 0L

        // Simulate unique ID and timestamp in the past
        `when`(mockSharedPreferences.getString(eq(KEY_UNIQUE_ID), any())).thenReturn(null)
        `when`(mockSharedPreferences.getLong(eq(KEY_TIMESTAMP), anyLong())).thenReturn(currentTime - (expiredTimeThresholdInHrs * 3600000) - 1)

        // Capture the arguments passed to putString
        val uniqueIdCaptor = ArgumentCaptor.forClass(String::class.java)

        // Act
        UniqueIdManager.initialize(mockContext, expiredTimeThresholdInHrs)
        UniqueIdManager.initialize(mockContext, neverTrackTimeThresholdInHrs)

        // Assert
        // Verify that putString was called thrice with the correct argument, and apply() was called thrice each
        verify(mockEditor, times(3)).putString(eq(KEY_UNIQUE_ID), uniqueIdCaptor.capture())
        verify(mockEditor, times(3)).putLong(eq(KEY_TIMESTAMP), anyLong())
        verify(mockEditor, times(6)).apply()

        // Assert the value passed to putString
        val generatedUniqueId = uniqueIdCaptor.value
        assertNotNull(generatedUniqueId) // Ensure the generated value is not null
    }

    //Test to verify the unique id generated is a valid 64 bit hex
    @Test
    fun `test verify unique id generated is a 64 bit hex and not empty`(){
        val validIdCharacters = "0123456789abcdef"
        val uniqueId = UniqueIdManager.generateUniqueIdImpl()
        assert(uniqueId.isNotEmpty())
        assertEquals(16, uniqueId.length)
        assert(uniqueId.all { it in validIdCharacters })
    }

    // More test cases can be added here...
}
