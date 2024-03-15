/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.activity

import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class FragmentActivityRegisterTest {

    @Mock
    private lateinit var mockActivity: FragmentActivity

    @Mock
    private lateinit var mockFragmentCallbacks: FragmentManager.FragmentLifecycleCallbacks

    private lateinit var fragmentActivityRegister: FragmentActivityRegister

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        fragmentActivityRegister = spy(FragmentActivityRegister())
    }

    @Test
    fun `test create should register fragmentCallbacks in onActivityPreCreated for FragmentActivity`() {
        val lifecycleCallbacks = fragmentActivityRegister.create(mockFragmentCallbacks)
        val mockFragmentManager = mock(FragmentManager::class.java)
        `when`(mockActivity.supportFragmentManager).thenReturn(mockFragmentManager)
        lifecycleCallbacks.onActivityPreCreated(mockActivity, null)
        verify(mockFragmentManager, times(1)).registerFragmentLifecycleCallbacks(
            mockFragmentCallbacks,
            true
        )
    }

    @Test
    fun `test create should not register fragmentCallbacks in onActivityPreCreated for non-FragmentActivity`() {
        val lifecycleCallbacks = fragmentActivityRegister.create(mockFragmentCallbacks)
        val mockFragmentManager = mock(FragmentManager::class.java)
        `when`(mockActivity.supportFragmentManager).thenReturn(mockFragmentManager)
        lifecycleCallbacks.onActivityPreCreated(mockActivity, null)
        verify(mockFragmentManager, atLeastOnce()).registerFragmentLifecycleCallbacks(
            mockFragmentCallbacks,
            true
        )
    }

    @Test
    fun `test createPre29 should register fragmentCallbacks in onActivityCreated for FragmentActivity`() {
        val lifecycleCallbacks = fragmentActivityRegister.createPre29(mockFragmentCallbacks)
        val mockFragmentManager = mock(FragmentManager::class.java)
        `when`(mockActivity.supportFragmentManager).thenReturn(mockFragmentManager)
        lifecycleCallbacks.onActivityCreated(mockActivity, null)
        verify(mockFragmentManager, times(1)).registerFragmentLifecycleCallbacks(
            mockFragmentCallbacks,
            true
        )
    }

    @Test
    fun `test createPre29 should not register fragmentCallbacks in onActivityCreated for non-FragmentActivity`() {
        val lifecycleCallbacks = fragmentActivityRegister.createPre29(mockFragmentCallbacks)
        val mockFragmentManager = mock(FragmentManager::class.java)
        `when`(mockActivity.supportFragmentManager).thenReturn(mockFragmentManager)
        val nonFragmentActivity = mock(ComponentActivity::class.java)
        lifecycleCallbacks.onActivityCreated(nonFragmentActivity, null)
        verify(mockFragmentManager, never()).registerFragmentLifecycleCallbacks(
            mockFragmentCallbacks,
            true
        )
    }

    @Test
    fun `test create should not register fragmentCallbacks in onActivityCreated for non-FragmentActivity f`() {
        val lifecycleCallbacks = fragmentActivityRegister.create(mockFragmentCallbacks)
        val mockFragmentManager = mock(FragmentManager::class.java)
        `when`(mockActivity.supportFragmentManager).thenReturn(mockFragmentManager)
        val nonFragmentActivity = mock(ComponentActivity::class.java)
        lifecycleCallbacks.onActivityPreCreated(nonFragmentActivity, null)
        verify(mockFragmentManager, never()).registerFragmentLifecycleCallbacks(
            mockFragmentCallbacks,
            true
        )
    }
}

