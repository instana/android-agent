/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.plugin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.gradle.api.logging.Logger
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class IgnoredClassesTest {

    @Test
    fun `test instanaLibraries regex`() {
        val regex = IgnoredClasses.instanaLibraries
        Assert.assertTrue(regex.matches("com.instana.android.someclass"))
        Assert.assertFalse(regex.matches("com.example.app.someclass"))
    }

    @Test
    fun `test troublesomeAnalytics regex`() {
        val regexList = IgnoredClasses.troublesomeAnalytics
        for (regex in regexList) {
            Assert.assertFalse(regex.matches("com.example.app.someclass"))
        }
    }

    @Test
    fun `test from method with valid regex`() {
        val loggerMock = Mockito.mock(Logger::class.java)
        val inputList = listOf("com\\.example\\.app\\..*", "com\\.instana\\.android\\..*")

        val result = IgnoredClasses.from(inputList, loggerMock)

        Assert.assertEquals(2, result.size)
        Assert.assertTrue(result[0].matches("com.example.app.someclass"))
        Assert.assertTrue(result[1].matches("com.instana.android.someclass"))
        verify(loggerMock, never())
    }

    @Test
    fun `test from method with invalid regex`() {
        val loggerMock = Mockito.mock(Logger::class.java)
        val inputList = listOf("com\\.example\\.app\\..*", "invalid[regex]")

        val result = IgnoredClasses.from(inputList, loggerMock)
        val result2 = IgnoredClasses.from(listOf("23466@#$%^&**(("), loggerMock)

        Assert.assertEquals(2, result.size)
        Assert.assertTrue(result[0].matches("com.example.app.someclass"))
        verify(loggerMock, atLeastOnce()).error(any())
    }
}
