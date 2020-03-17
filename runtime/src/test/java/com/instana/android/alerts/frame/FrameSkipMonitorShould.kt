package com.instana.android.alerts.frame

import android.view.Choreographer
import com.instana.android.BaseTest
import com.instana.android.performance.PerformanceMonitorConfig
import com.instana.android.performance.frame.FrameSkipMonitor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class FrameSkipMonitorShould : BaseTest() {

    private val mockChoreographer = mock<Choreographer>()

//    @Test
//    fun doFrame() {
//        val frameMonitor = FrameSkipMonitor(AlertsConfiguration(true), mockWorkManager, mockInstanaLifeCycle)
//        Handler().postDelayed({
//            frameMonitor.enableWithNoDelay()
//            TimeUnit.MILLISECONDS.sleep(100)
//            verify(mockInstanaLifeCycle).activityName
//            verify(mockWorkManager).send(any())
//        }, 300)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun inside_a_new_thread() {
//        val frameMonitor = FrameSkipMonitor(AlertsConfiguration(false), mockWorkManager, mockInstanaLifeCycle)
//        val activity = Robolectric.setupActivity(Activity::class.java)
//        val latch = CountDownLatch(1)
//        val didRun = AtomicBoolean(false)
//
//        val thread = Thread {
//            activity.runOnUiThread {
//                frameMonitor.enableWithNoDelay()
//                didRun.set(true)
//                latch.countDown()
//                Handler(activity.mainLooper).postDelayed({
//                    TimeUnit.MILLISECONDS.sleep(200)
//                }, 2000)
//            }
//        }
//        thread.start()
//
//        // sleep current thread to give the new thread a chance to run and post the runnable
//        TimeUnit.MILLISECONDS.sleep(2000)
//
//        // This method will cause the runnable to be executed
//        Robolectric.flushForegroundThreadScheduler()
//
//        // Should immediately return since the runnable has been executed
//        latch.await(20, TimeUnit.SECONDS)
//
//
//        assertTrue(didRun.get())
//        verify(mockInstanaLifeCycle).activityName
//        verify(mockWorkManager).send(any())
//    }

    @Test
    fun enable() {
        val frameMonitor = FrameSkipMonitor(
            PerformanceMonitorConfig(false), mockWorkManager, mockInstanaLifeCycle, mockChoreographer
        )
        frameMonitor.enable()
        verify(mockChoreographer).postFrameCallbackDelayed(frameMonitor, 4000L)
    }

    @Test
    fun disable() {
        val frameMonitor = FrameSkipMonitor(
            PerformanceMonitorConfig(true), mockWorkManager, mockInstanaLifeCycle, mockChoreographer
        )
        frameMonitor.disable()
        verify(mockChoreographer).removeFrameCallback(frameMonitor)
    }
}