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
import java.time.Instant

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

    internal fun invokePrivateMethod3(
        obj: Any,
        methodName: String,
        param1: Any?,
        paramType1: Class<*>,
        param2: Any?,
        paramType2: Class<*>
    ): Any? {
        // Retrieve the method with the matching parameter types
        val method: Method = obj.javaClass.getDeclaredMethod(methodName, paramType1, paramType2)

        // Convert the first primitive parameter to its wrapper class
        val modifiedParam1 = if (param1 != null && paramType1.isPrimitive) {
            when (param1) {
                is Int -> param1 as Int
                is Long -> param1 as Long
                is Double -> param1 as Double
                // Add other primitive types as needed
                else -> throw IllegalArgumentException("Unsupported primitive type: $paramType1")
            }
        } else {
            param1
        }

        // Convert the second primitive parameter to its wrapper class
        val modifiedParam2 = if (param2 != null && paramType2.isPrimitive) {
            when (param2) {
                is Int -> param2 as Int
                is Long -> param2 as Long
                is Double -> param2 as Double
                // Add other primitive types as needed
                else -> throw IllegalArgumentException("Unsupported primitive type: $paramType2")
            }
        } else {
            param2
        }

        method.isAccessible = true
        // Invoke the method with two parameters
        return method.invoke(obj, modifiedParam1, modifiedParam2)
    }


    internal fun createListOfFiles(directoryPath: String, fileCount: Int,olderNumberOfFiles:Int=0,modifiedAtMin:Long=15L): ArrayList<File> {
        val directory = Files.createTempDirectory(directoryPath).toFile()

        val files = arrayListOf<File>()
        val olderModifiedTime = Instant.now().minusSeconds(modifiedAtMin * 60).toEpochMilli()
        repeat(fileCount) { index ->
            val tempFile = createTempFile(directory, "testFile_$index", ".txt")
            Files.write(tempFile.toPath(), "Test content for file $index  :  ".toByteArray())
            if(olderNumberOfFiles!=0 && index<olderNumberOfFiles){
                tempFile.setLastModified(olderModifiedTime)
            }
            files.add(tempFile)
        }

        return files
    }

    internal fun createTempFile(directory: File, prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, directory)
    }
}