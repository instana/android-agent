/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.plugin

/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

import org.gradle.api.logging.Logger
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class ExtensionTest {

    @Mock
    lateinit var mockLogger: Logger

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testLogAll() {
        val ext = Extension().apply {
            ignoreClassesRegex = listOf("regex1", "regex2")
            logTimeSpent = true
            logVisits = false
            logInstrumentation = false
        }

        mockLogger.logAll(ext)

        val expectedLogs = listOf(
            "Plugin configuration ignoreClassesRegex: [regex1, regex2]",
            "Plugin configuration logTimeSpent: true",
            "Plugin configuration logVisits: false",
            "Plugin configuration logInstrumentation: false"
        )

        expectedLogs.forEachIndexed { index, expectedLog ->
            `when`(mockLogger.debug(expectedLog)).then {
                assertEquals(expectedLog, it.arguments[0])
                null
            }
        }
        expectedLogs.forEach {
            mockLogger.debug(it)
        }
    }
}
