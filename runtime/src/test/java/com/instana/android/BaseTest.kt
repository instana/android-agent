/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaWorkManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
@Config(minSdk = 31, maxSdk = 31)
abstract class BaseTest {
    internal val app = ApplicationProvider.getApplicationContext<Application>()
    internal val mockWorkManager = mock<InstanaWorkManager>()
    internal val mockInstanaLifeCycle = mock<InstanaLifeCycle> {
        on { activityName } doReturn "activity"
    }
    internal fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }

    internal fun getPrivateFieldValue(obj: Any, fieldName: String): Any? {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(obj)
    }

    internal fun invokePrivateMethod(obj: Any, methodName: String): Any? {
        val method: Method = obj.javaClass.getDeclaredMethod(methodName)
        method.isAccessible = true
        return method.invoke(obj)
    }

    internal fun invokePrivateMethod2(obj: Any, methodName: String, param: Any?, paramType: Class<*>): Any? {
        val method: Method = obj.javaClass.getDeclaredMethod(methodName, paramType)

        // Convert primitive types to their corresponding wrapper classes
        val modifiedParam = if (param != null && paramType.isPrimitive) {
            when (param) {
                is Int -> param as Int
                is Long -> param as Long
                // Add other primitive types as needed
                else -> throw IllegalArgumentException("Unsupported primitive type: $paramType")
            }
        } else {
            param
        }

        method.isAccessible = true
        return method.invoke(obj, modifiedParam)
    }

    internal fun createListOfFiles(directoryPath: String, fileCount: Int): ArrayList<File> {
        val directory = Files.createTempDirectory(directoryPath).toFile()

        val files = arrayListOf<File>()

        repeat(fileCount) { index ->
            val tempFile = createTempFile(directory, "testFile_$index", ".txt")
            Files.write(tempFile.toPath(), "Test content for file $index  :  ".toByteArray())
            files.add(tempFile)
        }

        return files
    }

    internal fun createTempFile(directory: File, prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, directory)
    }
}