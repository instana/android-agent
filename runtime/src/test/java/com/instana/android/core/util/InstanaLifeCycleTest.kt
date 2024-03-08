/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import android.app.Activity
import android.os.Bundle
import com.instana.android.BaseTest
import com.instana.android.core.InstanaLifeCycle
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.lang.NullPointerException

class InstanaLifeCycleTest:BaseTest() {

    @Mock
    lateinit var mockActivity: Activity

    @Mock
    lateinit var mockBundle: Bundle

    lateinit var instanaLifeCycle: InstanaLifeCycle

    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
        instanaLifeCycle = InstanaLifeCycle(app)
    }

    @Test
    fun `test onActivityPaused on the activity calls for the localClassName`(){
        try {
            instanaLifeCycle.onActivityPaused(activity = mockActivity)
            verify(mockActivity, atLeastOnce()).localClassName
        }catch (e:NullPointerException){
            verify(mockActivity, atLeastOnce()).localClassName
        }
    }

    @Test
    fun `test onActivityResumed on the activity calls for the localClassName`(){
        try {
            instanaLifeCycle.onActivityResumed(activity = mockActivity)
            verify(mockActivity, atLeastOnce()).localClassName
        }catch (e:NullPointerException){
            verify(mockActivity, atLeastOnce()).localClassName
        }
    }

    @Test
    fun `test onActivityStarted on the activity calls for the localClassName`(){
        try {
            instanaLifeCycle.onActivityStarted(activity = mockActivity)
            verify(mockActivity, atLeastOnce()).localClassName
        }catch (e:NullPointerException){
            verify(mockActivity, atLeastOnce()).localClassName
        }
    }

    @Test
    fun `test onActivityDestroyed on the activity calls for the localClassName`(){
        try {
            instanaLifeCycle.onActivityDestroyed(activity = mockActivity)
            verify(mockActivity, atLeastOnce()).localClassName
        }catch (e:NullPointerException){
            verify(mockActivity, atLeastOnce()).localClassName
        }
    }

    @Test
    fun `test onActivitySaveInstanceState on the activity calls for the localClassName`(){
        try {
            instanaLifeCycle.onActivitySaveInstanceState(activity = mockActivity,mockBundle)
            verify(mockActivity, atLeastOnce()).localClassName
        }catch (e:NullPointerException){
            verify(mockActivity, atLeastOnce()).localClassName
        }
    }

    @Test
    fun `test onActivityStopped on the activity calls for the localClassName`(){
        try {
            instanaLifeCycle.onActivityStopped(activity = mockActivity)
            verify(mockActivity, atLeastOnce()).localClassName
        }catch (e:NullPointerException){
            verify(mockActivity, atLeastOnce()).localClassName
        }
    }

    @Test
    fun `test onActivityCreated on the activity calls for the localClassName`(){
        try {
            instanaLifeCycle.onActivityCreated(activity = mockActivity,mockBundle)
            verify(mockActivity, atLeastOnce()).localClassName
        }catch (e:NullPointerException){
            verify(mockActivity, atLeastOnce()).localClassName
        }
    }

}