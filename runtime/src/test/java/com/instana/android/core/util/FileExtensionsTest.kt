/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import android.os.Build
import com.instana.android.BaseTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.powermock.reflect.Whitebox
import java.io.File

class FileExtensionsTest:BaseTest() {


    @Test
    fun `test file extension for isDirectoryEmpty`(){
        Whitebox.setInternalState(Build.VERSION::class.java,"SDK_INT",16)
        val directory = Mockito.mock(File::class.java)
        Mockito.`when`(directory.listFiles()).thenReturn(createListOfFiles("directory",4).toTypedArray())
        Assert.assertFalse(directory.isDirectoryEmpty())
    }

    @Test
    fun `test file extension for isDirectoryEmpty else part with empty directory`(){
        Whitebox.setInternalState(Build.VERSION::class.java,"SDK_INT",16)
        val directory = Mockito.mock(File::class.java)
        Mockito.`when`(directory.listFiles()).thenReturn(createListOfFiles("directory",0).toTypedArray())
        Assert.assertTrue(directory.isDirectoryEmpty())
    }


}