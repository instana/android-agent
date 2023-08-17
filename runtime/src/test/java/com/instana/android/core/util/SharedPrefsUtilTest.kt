/*
 * IBM Confidential
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.util

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the SharedPrefsUtil utility class.
 */
class SharedPrefsUtilTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    private lateinit var sharedPrefsUtil: SharedPrefsUtil

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        // Initialize Mockito annotations and mock objects
        MockitoAnnotations.initMocks(this)

        // Mock shared preferences behavior for the context
        Mockito.`when`(mockContext.getSharedPreferences(Mockito.anyString(), Mockito.anyInt()))
            .thenReturn(mockSharedPreferences)
        Mockito.`when`(mockSharedPreferences.edit()).thenReturn(mockEditor)

        // Create an instance of the SharedPrefsUtil utility class
        sharedPrefsUtil = SharedPrefsUtil
    }

    @Test
    fun testGetString() {
        val key = "testKey"
        val defaultValue = "default"
        val expectedValue = "testValue"

        // Mock the behavior of shared preferences to return the expected string value
        Mockito.`when`(mockSharedPreferences.getString(Mockito.eq(key), Mockito.anyString()))
            .thenReturn(expectedValue)

        // Perform the test by calling the getString function
        val result = sharedPrefsUtil.getString(mockContext, key, defaultValue)

        // Assert that the result matches the expected value
        assert(result == expectedValue)
    }

    @Test
    fun testPutString() {
        val key = "testKey"
        val value = "testValue"

        // Call the putString function to store a string value
        sharedPrefsUtil.putString(mockContext, key, value)

        // Verify that the appropriate putString and apply methods are called
        Mockito.verify(mockSharedPreferences.edit()).putString(key, value)
        Mockito.verify(mockSharedPreferences.edit()).apply()
    }

    @Test
    fun testGetLong() {
        val key = "testKey"
        val defaultValue = 42L
        val expectedValue = 123L

        // Mock the behavior of shared preferences to return the expected long value
        Mockito.`when`(mockSharedPreferences.getLong(Mockito.eq(key), Mockito.anyLong()))
            .thenReturn(expectedValue)

        // Perform the test by calling the getLong function
        val result = sharedPrefsUtil.getLong(mockContext, key, defaultValue)

        // Assert that the result matches the expected value
        assert(result == expectedValue)
    }

    @Test
    fun testPutLong() {
        val key = "testKey"
        val value = 123L

        // Call the putLong function to store a long value
        sharedPrefsUtil.putLong(mockContext, key, value)

        // Verify that the appropriate putLong and apply methods are called
        Mockito.verify(mockSharedPreferences.edit()).putLong(key, value)
        Mockito.verify(mockSharedPreferences.edit()).apply()
    }
}
