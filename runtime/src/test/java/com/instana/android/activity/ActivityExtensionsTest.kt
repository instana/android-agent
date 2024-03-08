/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.activity

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.instana.android.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ActivityExtensionsTest:BaseTest() {

    @Test
    fun `test findContentDescription returns contentDescription from rootView`() {
        val activityMock = mock(Activity::class.java)
        val rootViewMock = mock(View::class.java)
        `when`(activityMock.findViewById<View>(android.R.id.content)).thenReturn(rootViewMock)
        `when`(rootViewMock.contentDescription).thenReturn("RootView Content Description")
        val result = activityMock.findContentDescription()
        assertEquals("RootView Content Description", result)
    }

    @Test
    fun `test findContentDescription returns contentDescription from child view`() {
        val activityMock = mock(Activity::class.java)
        val rootViewMock = mock(ViewGroup::class.java)
        val childViewMock = mock(View::class.java)
        `when`(activityMock.findViewById<View>(android.R.id.content)).thenReturn(rootViewMock)
        `when`(rootViewMock.contentDescription).thenReturn(null)
        `when`(rootViewMock.childCount).thenReturn(1)
        `when`(rootViewMock.getChildAt(0)).thenReturn(childViewMock)
        `when`(childViewMock.contentDescription).thenReturn("ChildView Content Description")
        val result = activityMock.findContentDescription()
        assertEquals("ChildView Content Description", result)
    }

    @Test
    fun `test findContentDescription returns null when no contentDescription found`() {
        val activityMock = mock(Activity::class.java)
        val rootViewMock = mock(ViewGroup::class.java)
        `when`(activityMock.findViewById<View>(android.R.id.content)).thenReturn(rootViewMock)
        `when`(rootViewMock.contentDescription).thenReturn(null)
        `when`(rootViewMock.childCount).thenReturn(2)
        `when`(rootViewMock.getChildAt(0)).thenReturn(mock(View::class.java))
        `when`(rootViewMock.getChildAt(1)).thenReturn(mock(View::class.java))
        `when`(rootViewMock.getChildAt(0).contentDescription).thenReturn(null)
        `when`(rootViewMock.getChildAt(1).contentDescription).thenReturn(null)
        val result = activityMock.findContentDescription()
        assertEquals(null, result)
    }
}
