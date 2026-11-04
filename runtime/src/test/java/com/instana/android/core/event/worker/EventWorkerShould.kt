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
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
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
            null, Instana.config?.reportingURL, 0L, "tag"
        )
        val workSpec = workRequest.workSpec

        assertThat(workSpec.constraints.requiredNetworkType, `is`(equalTo(NetworkType.UNMETERED)))
        assertThat(workSpec.constraints.requiresBatteryNotLow(), `is`(equalTo(true)))
        assertThat(workSpec.constraints.requiresCharging(), `is`(equalTo(false)))
    }

    @Test
    fun doWorkEnqueued() {
        val directory = app.filesDir
        val request = EventWorker.createWorkRequest(Constraints.NONE, directory,
            null, Instana.config?.reportingURL, 0L, "tag"
        )

        val instanaWorkManager = Instana.workManager
        Assert.assertNotNull(instanaWorkManager)

        val workManager = instanaWorkManager!!.getWorkManager()
        Assert.assertNotNull(workManager)

        // Enqueue and wait for result.
        workManager!!.enqueue(request).result
        // Get WorkInfo
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo?.state, `is`(WorkInfo.State.ENQUEUED))
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
        // send() now takes (String, String) — second param is workerTag for logging
        val sendResult = invokePrivateMethod3(worker,"send","beacon string",String::class.java,"",String::class.java) as EventWorker.SendResult
        Assert.assertFalse(sendResult.success)
    }

    @Test
    fun `test send call should return boolean true`(){
        val worker = eventWorker
        setPrivateField(worker,"params",workParams)
        `when`(workParams.inputData).thenReturn(data)
        val sendResult = invokePrivateMethod3(worker,"send","beacon string",String::class.java,"",String::class.java) as EventWorker.SendResult
        Assert.assertTrue(sendResult.success)
    }

    @Test
    fun `test send call should return boolean true when reporting url is blank`(){
        val worker = eventWorker
        setPrivateField(worker,"params",workParams)
        `when`(workParams.inputData).thenReturn(data)
        `when`(workParams.inputData.getString("reporting_url")).thenReturn("")
        val sendResult = invokePrivateMethod3(worker,"send","beacon string",String::class.java,"",String::class.java) as EventWorker.SendResult
        Assert.assertTrue(sendResult.success)
    }

    @Test
    fun `test read all files should return`(){
        val mockWorkManager = mock(InstanaWorkManager::class.java)
        val directory = mock(File::class.java)
        val limit = 20
        // readAllFiles now takes (InstanaWorkManager?, File, File?, Int, String)
        val readAllFilesMethod = EventWorker::class.java.getDeclaredMethod("readAllFiles",
            InstanaWorkManager::class.java,
            File::class.java,
            File::class.java,
            Int::class.javaPrimitiveType,
            String::class.java)
        readAllFilesMethod.isAccessible = true
        `when`(directory.listFiles()).thenReturn(createListOfFiles("directory",4).toTypedArray())
        val result: Pair<String, Array<File>> = readAllFilesMethod.invoke(eventWorker, mockWorkManager, directory, null, limit, "") as Pair<String, Array<File>>
        assert(result.second.isNotEmpty())
    }

    @Test
    fun `test finaliseResult should return Result with work manager provided`(){
        val mockWorkManager = Instana.workManager
        val mockResult = mock(ListenableWorker.Result::class.java)
        // finaliseResult takes (InstanaWorkManager?, Result, String, String)
        val finaliseResultMethod = EventWorker::class.java.getDeclaredMethod("finaliseResult",
            InstanaWorkManager::class.java,
            ListenableWorker.Result::class.java,
            String::class.java,
            String::class.java)
        finaliseResultMethod.isAccessible = true
        val result = finaliseResultMethod.invoke(eventWorker, mockWorkManager, mockResult, "", "test") as ListenableWorker.Result
        Assert.assertNull(result.outputData)
    }

    @Test
    fun `test finaliseResult should return Result with work manager null`(){
        val mockResult = mock(ListenableWorker.Result::class.java)
        val finaliseResultMethod = EventWorker::class.java.getDeclaredMethod("finaliseResult",
            InstanaWorkManager::class.java,
            ListenableWorker.Result::class.java,
            String::class.java,
            String::class.java)
        finaliseResultMethod.isAccessible = true
        val result = finaliseResultMethod.invoke(eventWorker, null, mockResult, "", "test") as ListenableWorker.Result
        Assert.assertNull(result.outputData)
    }

    @Test
    fun `test stale beacon remover logic if greater than max limit times 3 then keep 900 most recent`(){
        val directory = createListOfFiles("directory",(1000*3)+1).toTypedArray()
        val result = invokePrivateMethod2(eventWorker,"staleBeaconsRemover",directory,Array<File>::class.java) as Array<File>
        // Softened from nuclear delete: keep 900 most-recent files (same policy as 1000-3000 band)
        assert(result.size == 900)
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

    @Test
    fun `test if removeOldFiles works with older time than 15 minutes`(){
        val directory = createListOfFiles("directory",3000,20).toTypedArray()
        val result = invokePrivateMethod3(eventWorker,"removeOldFiles",directory,Array<File>::class.java,15,Int::class.java) as Array<File>
        Assert.assertEquals(result.size,2980)
    }

    @Test
    fun `test if removeOldFiles works within 15 minutes files`(){
        val directory = createListOfFiles("directory",3000,0).toTypedArray()
        val result = invokePrivateMethod3(eventWorker,"removeOldFiles",directory,Array<File>::class.java,15,Int::class.java) as Array<File>
        Assert.assertEquals(result.size,3000)
    }

    // -----------------------------------------------------------------------
    // recoverStagingFiles — inflight-marker 3-state recovery tests
    // Uses a real EventWorker instance (not the mock) to call the internal method directly.
    // -----------------------------------------------------------------------

    /** Make a file look old enough that recoverStagingFiles treats it as stale (> 35 s). */
    private fun makeStale(file: File) {
        file.setLastModified(System.currentTimeMillis() - 60_000L)
    }

    /** Build a real EventWorker via TestListenableWorkerBuilder (no WorkerParameters hassle). */
    private fun realWorker(): EventWorker =
        androidx.work.testing.TestListenableWorkerBuilder<EventWorker>(app).build() as EventWorker

    @Test
    fun `recoverStagingFiles — plain stale non-crash beacon is moved to beaconsDir`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()

        val staleFile = File(stagingDir, "abc123")
        staleFile.writeText("beacon-data")
        makeStale(staleFile)

        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test")

        Assert.assertFalse("stale plain file should be removed from staging", staleFile.exists())
        Assert.assertTrue("stale plain file should be recovered to beaconsDir", File(beaconsDir, "abc123").exists())
    }

    @Test
    fun `recoverStagingFiles — strict dedup stale inflight non-crash beacon is deleted`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()

        val inflightFile = File(stagingDir, "abc123.inflight")
        inflightFile.writeText("beacon-data")
        makeStale(inflightFile)

        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test", strictDedup = true)

        Assert.assertFalse("stale inflight file should be deleted in strict mode", inflightFile.exists())
        Assert.assertFalse("stale inflight file must NOT appear in beaconsDir", File(beaconsDir, "abc123").exists())
        Assert.assertFalse("stale inflight file must NOT appear in beaconsDir as inflight", File(beaconsDir, "abc123.inflight").exists())
    }

    @Test
    fun `recoverStagingFiles — non-strict stale inflight beacon retries up to max 3 attempts`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()

        val freshInflight = File(stagingDir, "fresh_bid.inflight").also { it.writeText("data"); makeStale(it) }
        val r2Inflight = File(stagingDir, "bid_r2.inflight").also { it.writeText("data"); makeStale(it) }
        val r3Inflight = File(stagingDir, "bid_r3.inflight").also { it.writeText("data"); makeStale(it) }

        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test", strictDedup = false)

        // fresh_bid should be bumped to fresh_bid_r1 and moved to beaconsDir
        Assert.assertFalse(freshInflight.exists())
        Assert.assertTrue(File(beaconsDir, "fresh_bid_r1").exists())

        // bid_r2 should be bumped to bid_r3 and moved to beaconsDir
        Assert.assertFalse(r2Inflight.exists())
        Assert.assertTrue(File(beaconsDir, "bid_r3").exists())

        // bid_r3 has reached limit 3, so it should be deleted
        Assert.assertFalse(r3Inflight.exists())
        Assert.assertFalse(File(beaconsDir, "bid_r3.inflight").exists())
        Assert.assertFalse(File(beaconsDir, "bid_r4").exists())
    }

    @Test
    fun `recoverStagingFiles — stale crash beacon (plain) is always recovered`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()

        val crashFile = File(stagingDir, "cr_abc123")
        crashFile.writeText("crash-data")
        makeStale(crashFile)

        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test")

        Assert.assertFalse("stale crash file should be removed from staging", crashFile.exists())
        Assert.assertTrue("stale crash file should be recovered to beaconsDir", File(beaconsDir, "cr_abc123").exists())
    }

    @Test
    fun `recoverStagingFiles — stale crash beacon (inflight) is always recovered`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()

        val crashInflightFile = File(stagingDir, "cr_abc123.inflight")
        crashInflightFile.writeText("crash-data")
        makeStale(crashInflightFile)

        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test")

        Assert.assertFalse("stale crash inflight file should be removed from staging", crashInflightFile.exists())
        Assert.assertTrue("stale crash inflight file should be recovered to beaconsDir as plain", File(beaconsDir, "cr_abc123").exists())
    }

    @Test
    fun `recoverStagingFiles — fresh files (below threshold) are left untouched`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()

        val freshPlain = File(stagingDir, "fresh_plain")
        freshPlain.writeText("data")
        val freshInflight = File(stagingDir, "fresh_inflight.inflight")
        freshInflight.writeText("data")

        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test")

        Assert.assertTrue("fresh plain file should remain in staging", freshPlain.exists())
        Assert.assertTrue("fresh inflight file should remain in staging", freshInflight.exists())
        Assert.assertEquals("beaconsDir should be empty (no files recovered)", 0, beaconsDir.listFiles()?.size ?: 0)
    }

    @Test
    fun `recoverStagingFiles — empty staging directory does not throw`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()
        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test")
    }

    @Test
    fun `recoverStagingFiles — mix of stale plain, inflight, and crash files handled correctly`() {
        val stagingDir = createTempDir()
        val beaconsDir = createTempDir()

        val plain = File(stagingDir, "plain_beacon").also { it.writeText("d"); makeStale(it) }
        val inflight = File(stagingDir, "normal_beacon.inflight").also { it.writeText("d"); makeStale(it) }
        val crashPlain = File(stagingDir, "cr_crash1").also { it.writeText("d"); makeStale(it) }
        val crashInflight = File(stagingDir, "cr_crash2.inflight").also { it.writeText("d"); makeStale(it) }
        val freshPlain = File(stagingDir, "fresh").also { it.writeText("d") }

        realWorker().recoverStagingFiles(stagingDir, beaconsDir, "test", strictDedup = true)

        Assert.assertFalse(plain.exists())
        Assert.assertTrue(File(beaconsDir, "plain_beacon").exists())

        Assert.assertFalse(inflight.exists())
        Assert.assertFalse(File(beaconsDir, "normal_beacon").exists())

        Assert.assertFalse(crashPlain.exists())
        Assert.assertTrue(File(beaconsDir, "cr_crash1").exists())

        Assert.assertFalse(crashInflight.exists())
        Assert.assertTrue(File(beaconsDir, "cr_crash2").exists())

        Assert.assertTrue(freshPlain.exists())
    }

}
