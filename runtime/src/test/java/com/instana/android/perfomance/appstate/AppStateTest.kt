package com.instana.android.perfomance.appstate

import com.instana.android.performance.appstate.AppState
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AppStateTest {

    @Test
    fun testEnumValues() {
        // Verify that the enum has the correct values.
        assertEquals(AppState.FOREGROUND.value, "f")
        assertEquals(AppState.BACKGROUND.value, "b")
        assertEquals(AppState.UN_IDENTIFIED.value, "u")
    }

    @Test
    fun testEnumName() {
        // Verify that the enum has the correct names.
        assertEquals(AppState.FOREGROUND.name, "FOREGROUND")
        assertEquals(AppState.BACKGROUND.name, "BACKGROUND")
        assertEquals(AppState.UN_IDENTIFIED.name, "UN_IDENTIFIED")
    }
}