/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE
import android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaLifeCycle
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.powermock.reflect.Whitebox

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

    @Test
    fun `test onActivityPaused testing activityname`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        whenever(mockActivity.localClassName).thenReturn("Activity")
        instanaLifeCycle.onActivityPaused(mockActivity)
        assert(instanaLifeCycle.activityName == "Activity")
    }

    @Test
    fun `test onActivityResumed testing activityname`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        whenever(mockActivity.localClassName).thenReturn("ActivityResumed")
        instanaLifeCycle.onActivityResumed(mockActivity)
        assert(instanaLifeCycle.activityName == "ActivityResumed")
    }

    @Test
    fun `test onActivityStarted testing activityname`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        whenever(mockActivity.localClassName).thenReturn("onActivityStarted")
        instanaLifeCycle.onActivityStarted(mockActivity)
        assert(instanaLifeCycle.activityName == "onActivityStarted")
    }

    @Test
    fun `test onActivityDestroyed testing activityname`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        whenever(mockActivity.localClassName).thenReturn("onActivityDestroyed")
        instanaLifeCycle.onActivityDestroyed(mockActivity)
        assert(instanaLifeCycle.activityName == "onActivityDestroyed")
    }

    @Test
    fun `test onActivitySaveInstanceState testing activityname`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        whenever(mockActivity.localClassName).thenReturn("onActivitySaveInstanceState")
        instanaLifeCycle.onActivitySaveInstanceState(mockActivity,mockBundle)
        assert(instanaLifeCycle.activityName == "onActivitySaveInstanceState")
    }

    @Test
    fun `test onActivityCreated testing activityname`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        whenever(mockActivity.localClassName).thenReturn("onActivityCreated")
        instanaLifeCycle.onActivityCreated(mockActivity,mockBundle)
        assert(instanaLifeCycle.activityName == "onActivityCreated")
    }

    @Test
    fun `test onActivityStopped testing activityname`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        instanaLifeCycle.onLowMemory()
        instanaLifeCycle.onConfigurationChanged(Configuration())
        whenever(mockActivity.localClassName).thenReturn("onActivityStopped")
        instanaLifeCycle.onActivityStopped(mockActivity)
        assert(instanaLifeCycle.activityName == "onActivityStopped")
    }
    
    @Test
    fun `test onTrimMemory setting background as true if TRIM_MEMORY_UI_HIDDEN`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        instanaLifeCycle.onTrimMemory(TRIM_MEMORY_UI_HIDDEN)
        val backgroundBoolean = getPrivateFieldValue(instanaLifeCycle,"backgrounded") as Boolean
        assert(backgroundBoolean)
    }

    @Test
    fun `test onTrimMemory setting background as true if not TRIM_MEMORY_UI_HIDDEN`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        instanaLifeCycle.onTrimMemory(TRIM_MEMORY_COMPLETE)
        val backgroundBoolean = getPrivateFieldValue(instanaLifeCycle,"backgrounded") as Boolean
        Assert.assertFalse(backgroundBoolean)
    }

    @Test
    fun `test onActivityResumed should update background value`(){
        val instanaLifeCycle = InstanaLifeCycle(app)
        setPrivateField(instanaLifeCycle,"backgrounded",true)
        val beforeResumeBackgroundValue = getPrivateFieldValue(instanaLifeCycle,"backgrounded") as Boolean
        Assert.assertTrue(beforeResumeBackgroundValue)
        whenever(mockActivity.localClassName).thenReturn("ActivityResumed")
        instanaLifeCycle.onActivityResumed(mockActivity)
        val afterResumeBackgroundValue = getPrivateFieldValue(instanaLifeCycle,"backgrounded") as Boolean
        Assert.assertFalse(afterResumeBackgroundValue)
        assert(instanaLifeCycle.activityName == "ActivityResumed")
    }

    @Test
    fun `test autoCaptureScreenNames active should init callbacks form lifecycle should not affect on trim memory`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, autoCaptureScreenNames = true)
        Instana.setup(app, config)
        val instanaLifeCycle = InstanaLifeCycle(app)
        instanaLifeCycle.onTrimMemory(TRIM_MEMORY_UI_HIDDEN)
        val backgroundBoolean = getPrivateFieldValue(instanaLifeCycle,"backgrounded") as Boolean
        assert(backgroundBoolean)
    }

    @Test
    fun `test autoCaptureScreenNames active should work even build less than 29`(){
        Whitebox.setInternalState(Build.VERSION::class.java,"SDK_INT",30)
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, autoCaptureScreenNames = true)
        Instana.setup(app, config)
        val instanaLifeCycle = InstanaLifeCycle(app)
        instanaLifeCycle.onTrimMemory(TRIM_MEMORY_UI_HIDDEN)
        val backgroundBoolean = getPrivateFieldValue(instanaLifeCycle,"backgrounded") as Boolean
        assert(backgroundBoolean)
        Whitebox.setInternalState(Build.VERSION::class.java,"SDK_INT",23)
    }

    @Test
    fun `test registerFragmentCallbacks should call pre29 when build version is less than 29`(){
        val mockApp = mock(Application::class.java)
        Whitebox.setInternalState(Build.VERSION::class.java,"SDK_INT",23)
        val instanaLifeCycle = InstanaLifeCycle(mockApp)
        invokePrivateMethod2(instanaLifeCycle,"registerFragmentCallbacks",mockApp,Application::class.java)
        verify(mockApp, atLeastOnce()).registerActivityLifecycleCallbacks(any())
    }




}