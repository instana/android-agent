/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.fragments

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

private class FragmentTest:Fragment()
@RunWith(MockitoJUnitRunner::class)
class FragmentExtensionsTest {

    @Test
    fun `getLocalPathName should return correct local path name`() {
        val mockFragment = FragmentTest()
        val result = mockFragment.getLocalPathName()
        assertEquals("com.instana.android.fragments.FragmentTest", result)
    }

    @Test
    fun `findContentDescription should return tag if not null`() {
        val mockFragment = mock(Fragment::class.java)
        `when`(mockFragment.tag).thenReturn("TagValue")
        val result = mockFragment.findContentDescription()
        assertEquals("TagValue", result)
    }

    @Test
    fun `findContentDescription should return view content description if not null`() {
        val mockFragment = mock(Fragment::class.java)
        val mockView = mock(View::class.java)
        `when`(mockFragment.view).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("ViewContentDescription")
        val result = mockFragment.findContentDescription()
        assertEquals("ViewContentDescription", result)
    }

    @Test
    fun `findContentDescription should return view content description is null`() {
        val mockFragment = mock(Fragment::class.java)
        val mockView = mock(View::class.java)
        `when`(mockFragment.view).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn(null)
        val result = mockFragment.findContentDescription()
        assertEquals(null, result)
    }

    @Test
    fun `findContentDescription should return child view content description`() {
        val mockFragment = mock(Fragment::class.java)
        val mockViewGroup = mock(ViewGroup::class.java)
        val mockChildView1 = mock(View::class.java)
        val mockChildView2 = mock(View::class.java)

        `when`(mockFragment.view).thenReturn(mockViewGroup)
        `when`(mockViewGroup.childCount).thenReturn(2)
        `when`(mockViewGroup.getChildAt(0)).thenReturn(mockChildView1)
        `when`(mockViewGroup.getChildAt(1)).thenReturn(mockChildView2)
        `when`(mockChildView1.contentDescription).thenReturn(null)
        `when`(mockChildView2.contentDescription).thenReturn("ChildView2ContentDescription")
        val result = mockFragment.findContentDescription()

        assertEquals("ChildView2ContentDescription", result)
    }

    @Test
    fun `findContentDescription should return child first view content description`() {
        val mockFragment = mock(Fragment::class.java)
        val mockViewGroup = mock(ViewGroup::class.java)
        val mockChildView1 = mock(View::class.java)
        val mockChildView2 = mock(View::class.java)

        `when`(mockFragment.view).thenReturn(mockViewGroup)
        `when`(mockViewGroup.childCount).thenReturn(2)
        `when`(mockViewGroup.getChildAt(0)).thenReturn(mockChildView1)
        `when`(mockViewGroup.getChildAt(1)).thenReturn(mockChildView2)
        `when`(mockChildView1.contentDescription).thenReturn("ChildView2ContentDescriptionFirst")
        `when`(mockChildView2.contentDescription).thenReturn("ChildView2ContentDescription")
        val result = mockFragment.findContentDescription()

        assertEquals("ChildView2ContentDescriptionFirst", result)
    }

    @Test
    fun `findContentDescription should return null if no content description found`() {
        val mockFragment = mock(Fragment::class.java)
        `when`(mockFragment.view).thenReturn(null)
        val result = mockFragment.findContentDescription()
        assertEquals(null, result)
    }
}
