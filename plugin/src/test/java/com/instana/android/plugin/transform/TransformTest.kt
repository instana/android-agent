/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.plugin.transform

import org.junit.Test
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.junit.Assert.assertEquals
import org.mockito.Mockito.mock

class TransformTest {
    
    @Test
    fun `test DebugUtil value`(){
        assert(DebugUtil.firstInstrument)
    }

    @Test
    fun testIgnoresProperty() {
        // Create a mock ListProperty<Regex>
        val mockListProperty: ListProperty<Regex> = mock(ListProperty::class.java) as ListProperty<Regex>
        val mockProperty: Property<Boolean> = mock(Property::class.java) as Property<Boolean>
        // Create an instance of InstanaInstruParams with the mock ListProperty
        val instanaInstruParams = object : Transform.InstanaInstruParams {
            override val ignores: ListProperty<Regex>
                get() = mockListProperty
            override val logVisits: Property<Boolean>
                get() = mockProperty
            override val logInstrumentation: Property<Boolean>
                get() = mockProperty
        }

        // Assert that the ignores property returns the mockListProperty
        assertEquals(mockListProperty, instanaInstruParams.ignores)
    }

    @Test
    fun testLogVisitsProperty() {
        // Create a mock Property<Boolean>
        val mockProperty: Property<Boolean> = mock(Property::class.java) as Property<Boolean>

        // Create an instance of InstanaInstruParams with the mock Property
        val instanaInstruParams = object : Transform.InstanaInstruParams {
            override val ignores: ListProperty<Regex>
                get() = mock(ListProperty::class.java) as ListProperty<Regex>
            override val logVisits: Property<Boolean>
                get() = mockProperty
            override val logInstrumentation: Property<Boolean>
                get() = mockProperty
        }

        // Assert that the logVisits property returns the mockProperty
        assertEquals(mockProperty, instanaInstruParams.logVisits)
    }

    @Test
    fun testLogInstrumentationProperty() {
        // Create a mock Property<Boolean>
        val mockProperty: Property<Boolean> = mock(Property::class.java) as Property<Boolean>

        // Create an instance of InstanaInstruParams with the mock Property
        val instanaInstruParams = object : Transform.InstanaInstruParams {
            override val ignores: ListProperty<Regex>
                get() = mock(ListProperty::class.java) as ListProperty<Regex>
            override val logVisits: Property<Boolean>
                get() = mockProperty
            override val logInstrumentation: Property<Boolean>
                get() = mockProperty
        }

        // Assert that the logInstrumentation property returns the mockProperty
        assertEquals(mockProperty, instanaInstruParams.logInstrumentation)
    }
}