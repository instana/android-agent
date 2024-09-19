/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtensionsTest {


    @Test
    fun `test removeTrailing() removes the suffix correctly`() {
        val originalString = "example-suffix"
        val suffix = "-suffix"
        val result = originalString.removeTrailing(suffix)
        assertEquals("example", result)
    }

    @Test
    fun `test removeTrailing() handles strings without the specified suffix`() {
        val originalString = "example"
        val suffix = "-suffix"
        val result = originalString.removeTrailing(suffix)
        assertEquals(originalString, result)
    }

    @Test
    fun `test removeTrailing() handles empty string`() {
        val originalString = ""
        val suffix = "-suffix"
        val result = originalString.removeTrailing(suffix)
        assertEquals(originalString, result)
    }

    @Test
    fun `test extractBeaconValues with valid key`() {
        val input = """
            ro false
            vw 1440
            vh 3120
            ti 1722492818238
            d 0
            ec 0
            agv 6.0.19
            av 1.4.3
            ab 1
            bi com.ibm.instashop
            p Android
            osn Android
            osv 14 (34)
            dma Google
            dmo sdk_gphone64_arm64
            dh ranchu
            ul en-US
            ui 23323@32323
            un ttttt
            ue ttttt@email
            bid 1589b438d556000a
            k FrZ_vBXFRNWR3Ev_uhaGxQ
            sid 84d2ec10-572d-46b3-9e36-4197d4ddc0ec
            t viewChange
            usi 1c55dc2a-c462-4998-820c-d6b18ed61856
            uf c,sn
            v sign_in_screen
            im_act.resume.time 3477992281239
            im_act.class.name MainActivity
            im_act.local.path.name MainActivity
            im_act.screen.name MainActivity
        """.trimIndent()

        assertEquals("1722492818238", input.extractBeaconValues("ti"))
        assertEquals("6.0.19", input.extractBeaconValues("agv"))
        assertEquals(null, input.extractBeaconValues("nonexistent_key"))
    }

    @Test
    fun `test randomAlphaNumericString with default length`() {
        val length = 6
        val randomString = String.randomAlphaNumericString()

        assertTrue("Random string should be of length $length", randomString.length == length)
        assertTrue("Random string should contain only alphanumeric characters",
            randomString.all { it.isLetterOrDigit() })
    }

    @Test
    fun `test randomAlphaNumericString with specified length`() {
        val length = 10
        val randomString = String.randomAlphaNumericString(length)

        assertTrue("Random string should be of length $length", randomString.length == length)
        assertTrue("Random string should contain only alphanumeric characters",
            randomString.all { it.isLetterOrDigit() })
    }

    @Test
    fun `test randomAlphaNumericString with zero length`() {
        val length = 0
        val randomString = String.randomAlphaNumericString(length)

        assertTrue("Random string should be empty for length $length", randomString.isEmpty())
    }

    @Test
    fun `test randomAlphaNumericString with negative length`() {
        val length = -5
        val randomString = String.randomAlphaNumericString(length)

        // Expect empty string for negative length
        assertTrue("Random string should be empty for length $length", randomString.isEmpty())
    }

    @Test
    fun `test randomAlphaNumericString random generation`() {
        val length = 6
        val iterations = 100
        val uniqueStrings = mutableSetOf<String>()

        repeat(iterations) {
            val randomString = String.randomAlphaNumericString(length)
            uniqueStrings.add(randomString)
        }

        // Assuming a reasonable number of unique strings, it should be greater than 1
        assertTrue("There should be multiple unique strings generated", uniqueStrings.size > 1)
    }
}