/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.view

import com.instana.android.BaseTest
import com.instana.android.Instana
import org.junit.Assert
import org.junit.Test


class InstanaComposableUiObserverTest:BaseTest() {

    @Test
    fun `test Instana view will be set when the updateScreenName is called`(){
        val obj = InstanaComposableUiObserver;
        invokePrivateMethod2(obj,"updateScreenName","NEW_NAME",String::class.java)
        Assert.assertEquals("NEW_NAME",Instana.view)
    }
}