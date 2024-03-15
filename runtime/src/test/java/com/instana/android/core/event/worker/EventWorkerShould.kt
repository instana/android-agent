/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.event.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File

class EventWorkerShould : BaseTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var workParams:WorkerParameters

    @Mock
    lateinit var eventWorker: EventWorker

    @Mock
    lateinit var data: Data

    @Before
    fun `test setup`(){
        Instana.setup(app, InstanaConfig(API_KEY, SERVER_URL))
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun doWorkConstraintsSet() {
        val workerConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .build()
        val directory = app.filesDir
        val workRequest: WorkRequest = EventWorker.createWorkRequest(workerConstraint, directory,
            Instana.config?.reportingURL, false, 0L, "tag")
        val workSpec = workRequest.workSpec

        assertThat(workSpec.constraints.requiredNetworkType, `is`(equalTo(NetworkType.UNMETERED)))
        assertThat(workSpec.constraints.requiresBatteryNotLow(), `is`(equalTo(true)))
        assertThat(workSpec.constraints.requiresCharging(), `is`(equalTo(false)))
    }

    @Test
    fun doWorkEnqueued() {
        val directory = app.filesDir
        val request = EventWorker.createWorkRequest(Constraints.NONE, directory,
            Instana.config?.reportingURL, false,0L, "tag")

        val instanaWorkManager = Instana.workManager
        Assert.assertNotNull(instanaWorkManager)

        val workManager = instanaWorkManager!!.getWorkManager()
        Assert.assertNotNull(workManager)

        // Enqueue and wait for result.
        workManager!!.enqueue(request).result
        // Get WorkInfo
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }

    @Test
    fun `test doWork result null when directoryAbsPath is null or blank`() {
        val result = runBlocking {
            eventWorker.doWork()
        }
        assertEquals(null, result)
    }

    @Test
    fun `test send call should return boolean false`(){
        val worker = eventWorker
        setPrivateField(worker,"params",workParams)
        `when`(workParams.inputData).thenReturn(data)
        `when`(data.getString("reporting_url")).thenReturn(SERVER_URL)
        val booleanData = invokePrivateMethod2(worker,"send","beacon string",String::class.java)
        Assert.assertFalse(booleanData as Boolean)
    }

    @Test
    fun `test send call should return boolean true`(){
        val worker = eventWorker
        setPrivateField(worker,"params",workParams)
        `when`(workParams.inputData).thenReturn(data)
        val booleanData = invokePrivateMethod2(worker,"send","beacon string",String::class.java)
        Assert.assertTrue(booleanData as Boolean)
    }

    @Test
    fun `test send call should return boolean true when reporting url is blank`(){
        val worker = eventWorker
        setPrivateField(worker,"params",workParams)
        `when`(workParams.inputData).thenReturn(data)
        `when`(workParams.inputData.getString("reporting_url")).thenReturn("")
        val booleanData = invokePrivateMethod2(worker,"send","beacon string",String::class.java)
        Assert.assertTrue(booleanData as Boolean)
    }

    @Test
    fun `test read all files should return`(){
        val mockWorkManager = mock(InstanaWorkManager::class.java)
        val directory = mock(File::class.java)
        val limit = 20
        val readAllFilesMethod = EventWorker::class.java.getDeclaredMethod("readAllFiles",
            InstanaWorkManager::class.java,
            File::class.java,
            Int::class.javaPrimitiveType)
        readAllFilesMethod.isAccessible = true
        `when`(directory.listFiles()).thenReturn(createListOfFiles("directory",4).toTypedArray())
        val result: Pair<String, Array<File>> = readAllFilesMethod.invoke(eventWorker, mockWorkManager, directory, limit) as Pair<String, Array<File>>
        assert(result.first.contains("m_slowSendStartTime"))
        assert(result.second.isNotEmpty())

    }

    @Test
    fun `test makeResult should return Result with work manager provided`(){
        val mockWorkManager = Instana.workManager
        val mockResult = mock(ListenableWorker.Result::class.java)
        val makeResultMethod = EventWorker::class.java.getDeclaredMethod("makeResult",
            InstanaWorkManager::class.java,
            ListenableWorker.Result::class.java)
        makeResultMethod.isAccessible = true
        val result = makeResultMethod.invoke(eventWorker,mockWorkManager,mockResult) as ListenableWorker.Result
        Assert.assertNull(result.outputData)
    }

    @Test
    fun `test makeResult should return Result with work manager null`(){
        val mockResult = mock(ListenableWorker.Result::class.java)
        val makeResultMethod = EventWorker::class.java.getDeclaredMethod("makeResult",
            InstanaWorkManager::class.java,
            ListenableWorker.Result::class.java)
        makeResultMethod.isAccessible = true
        val result = makeResultMethod.invoke(eventWorker,null,mockResult) as ListenableWorker.Result
        Assert.assertNull(result.outputData)
    }

    @Test
    fun `test scheduleFlushAgain checks on work manager in slow send mode`(){
        val scheduleFlushAgainMethod = EventWorker::class.java.getDeclaredMethod("scheduleFlushAgain",
            InstanaWorkManager::class.java,
            Boolean::class.java,
            Boolean::class.java)
        scheduleFlushAgainMethod.isAccessible = true
        val mockWorkManager = mock(InstanaWorkManager::class.java)
        val mockInternalWorkManager = mock(WorkManager::class.java)
        `when`(mockWorkManager.getWorkManager()).thenReturn(mockInternalWorkManager)
        `when`(mockWorkManager.isInSlowSendMode()).thenReturn(true)
        scheduleFlushAgainMethod.invoke(eventWorker,mockWorkManager,true,true)
        verify(mockWorkManager, atLeastOnce()).flush(mockInternalWorkManager)
    }

    @Test
    fun `test scheduleFlushAgain checks on work manager in slow send mode before flush`(){
        val scheduleFlushAgainMethod = EventWorker::class.java.getDeclaredMethod("scheduleFlushAgain",
            InstanaWorkManager::class.java,
            Boolean::class.java,
            Boolean::class.java)
        scheduleFlushAgainMethod.isAccessible = true
        val mockWorkManager = mock(InstanaWorkManager::class.java)
        val mockInternalWorkManager = mock(WorkManager::class.java)
        `when`(mockWorkManager.getWorkManager()).thenReturn(mockInternalWorkManager)
        `when`(mockWorkManager.isInSlowSendMode()).thenReturn(false)
        scheduleFlushAgainMethod.invoke(eventWorker,mockWorkManager,true,true)
        verify(mockWorkManager, atLeastOnce()).flush(mockInternalWorkManager)
    }

    @Test
    fun `test scheduleFlushAgain checks on work manager in slow send with reachedBatchLimit`(){
        val scheduleFlushAgainMethod = EventWorker::class.java.getDeclaredMethod("scheduleFlushAgain",
            InstanaWorkManager::class.java,
            Boolean::class.java,
            Boolean::class.java)
        scheduleFlushAgainMethod.isAccessible = true
        val mockWorkManager = mock(InstanaWorkManager::class.java)
        val mockInternalWorkManager = mock(WorkManager::class.java)
        `when`(mockWorkManager.getWorkManager()).thenReturn(mockInternalWorkManager)
        `when`(mockWorkManager.isInSlowSendMode()).thenReturn(false)
        scheduleFlushAgainMethod.invoke(eventWorker,mockWorkManager,false,true)
        verify(mockWorkManager, atLeastOnce()).flush(mockInternalWorkManager)
    }

    @Test
    fun `test scheduleFlushAgain checks on work manager in slow send with empty message`(){
        val scheduleFlushAgainMethod = EventWorker::class.java.getDeclaredMethod("scheduleFlushAgain",
            InstanaWorkManager::class.java,
            Boolean::class.java,
            Boolean::class.java)
        scheduleFlushAgainMethod.isAccessible = true
        val mockWorkManager = mock(InstanaWorkManager::class.java)
        val mockInternalWorkManager = mock(WorkManager::class.java)
        `when`(mockWorkManager.getWorkManager()).thenReturn(mockInternalWorkManager)
        `when`(mockWorkManager.isInSlowSendMode()).thenReturn(false)
        scheduleFlushAgainMethod.invoke(eventWorker,mockWorkManager,false,false)
        verify(mockWorkManager, never()).flush(mockInternalWorkManager)
    }

    @Test
    fun `test scheduleFlushAgain checks on work manager is null`(){
        val scheduleFlushAgainMethod = EventWorker::class.java.getDeclaredMethod("scheduleFlushAgain",
            InstanaWorkManager::class.java,
            Boolean::class.java,
            Boolean::class.java)
        scheduleFlushAgainMethod.isAccessible = true
        val mockWorkManager = mock(InstanaWorkManager::class.java)
        val mockInternalWorkManager = mock(WorkManager::class.java)
        `when`(mockWorkManager.getWorkManager()).thenReturn(null)
        `when`(mockWorkManager.isInSlowSendMode()).thenReturn(false)
        scheduleFlushAgainMethod.invoke(eventWorker,mockWorkManager,false,false)
        verify(mockWorkManager, never()).flush(mockInternalWorkManager)
    }

    @Test
    fun `test stale beacon remover logic if greater than max limit times 3 then return empty list by discarding all beacons`(){
        val directory = createListOfFiles("directory",(1000*3)+1).toTypedArray()
        val result = invokePrivateMethod2(eventWorker,"staleBeaconsRemover",directory,Array<File>::class.java) as Array<File>
        assert(result.size==0)
    }

    @Test
    fun `test stale beacon remover logic if greater than max limit and less than maxlimit times 3 then take recent 900`(){
        val directory = createListOfFiles("directory",2999).toTypedArray()
        val result = invokePrivateMethod2(eventWorker,"staleBeaconsRemover",directory,Array<File>::class.java) as Array<File>
        assert(result.size==900)
    }

    @Test
    fun `test stale beacon remover logic if greater than max limit and less than maxlimit times 3 then take recent 900 condition 2`(){
        val directory = createListOfFiles("directory",1000).toTypedArray()
        val result = invokePrivateMethod2(eventWorker,"staleBeaconsRemover",directory,Array<File>::class.java) as Array<File>
        assert(result.size==900)
    }

    @Test
    fun `test stale beacon remover logic if greater than max limit and less than maxlimit times 3 then take recent 900 condition 3`(){
        val directory = createListOfFiles("directory",3000).toTypedArray()
        val result = invokePrivateMethod2(eventWorker,"staleBeaconsRemover",directory,Array<File>::class.java) as Array<File>
        Assert.assertEquals(result.size,3000)
    }

}
