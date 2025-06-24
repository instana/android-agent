/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.perfomance.network

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.instana.android.BaseTest
import com.instana.android.core.util.SharedPrefsUtil
import com.instana.android.performance.network.NetworkUsageStorageHelper
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockedStatic
import org.mockito.MockitoAnnotations

class NetworkUsageStorageHelperTest: BaseTest() {

    @Mock
    lateinit var application: Application // Mock the Application context
    @Mock
    lateinit var sharedPreferences: SharedPreferences // Mock SharedPreferences

    private lateinit var helper: NetworkUsageStorageHelper
    private lateinit var staticPrefsUtil: MockedStatic<SharedPrefsUtil>
    @Mock
    lateinit var editor: SharedPreferences.Editor // Mock SharedPreferences.Editor

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this) // Initialize mocks
        //SharedPrefsUtil.getSharedPreferences(application)
        // Mock the behavior of getSharedPreferences to return the mocked SharedPreferences
        `when`(invokePrivateMethod2(SharedPrefsUtil,"getSharedPreferences",application, Context::class.java)).thenReturn(sharedPreferences)
        // Mock SharedPreferences to return a mocked Editor
        `when`(sharedPreferences.edit()).thenReturn(editor)


        // Mock the behavior of putLong and commit to return the right values
        `when`(editor.putLong(anyString(), anyLong())).thenReturn(editor) // Ensure putLong returns the editor itself for chaining
        `when`(editor.commit()).thenReturn(true) // Simulate a successful commit

        helper = NetworkUsageStorageHelper(application) // Pass the mock application
        staticPrefsUtil = mockStatic(SharedPrefsUtil::class.java) // Mock SharedPrefsUtil static methods
    }

    @After
    fun tearDown() {
        staticPrefsUtil.close()
    }

    @Test
    fun `getDataUsed returns stored value`() {
        // Mock SharedPreferences.getLong() to return a value
        `when`(sharedPreferences.getLong("data_used_till_date", 0L)).thenReturn(12345L)

        val result = helper.getDataUsed()

        // Assert that the returned value is correct
        assert(result == 12345L)
    }


    @Test
    fun `isRebooted returns false when stored data is less than current`() {
        `when` (SharedPrefsUtil.getLong(application, "data_used_till_date", 0L))
            .thenReturn(1000L)

        val result = helper.isRebooted(5000L)

        assert(!result)
    }

    @Test
    fun `getBackgroundNetworkUsage returns stored value`() {
        `when` (SharedPrefsUtil.getLong(application, "instana_background_n_w_usage", 0L) )
            .thenReturn(2222L)

        val result = helper.getBackgroundNetworkUsage()

        assert(result == 2222L)
    }

}