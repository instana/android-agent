package com.instana.mobileeum

import android.content.ComponentCallbacks2
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.instana.android.Instana
import kotlinx.android.synthetic.main.activity_kotlin.*

class KotlinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)

        ok_http_put.setOnClickListener {
            Thread {
                OkHttpRequests.executePut(enableManual = false)
            }.start()
        }

        ok_http_delete.setOnClickListener {
            Thread {
                OkHttpRequests.executeDelete(enableManual = true)
            }.start()
        }

        ok_http_get.setOnClickListener {
            Thread {
                OkHttpRequests.executeGet(enableManual = false)
            }.start()
        }

        ok_http_post.setOnClickListener {
            // check if work manager works for client app
            val compressionWork =
                    OneTimeWorkRequest.Builder(TestWorker::class.java).build()
            WorkManager.getInstance().enqueue(compressionWork)
        }

        http_url_put.setOnClickListener {
            Thread { HttpUrlConnectionRequests.doPut(enableManual = false) }.start()
        }

        http_url_delete.setOnClickListener {
            Thread { HttpUrlConnectionRequests.doDelete(enableManual = false) }.start()
        }

        http_url_get.setOnClickListener {
            FetchRestData().execute()
        }

        http_url_post.setOnClickListener {
            Thread { HttpUrlConnectionRequests.doPost(enableManual = false) }.start()
        }

        crash_me_kotlin_button.setOnClickListener {
            val willCrash: String? = null
            willCrash!!.indexOf("crash")
        }

        crash_me_different_button.setOnClickListener {
            throw ArrayIndexOutOfBoundsException("crash2")
        }

        invoke_low_memory.setOnClickListener {
            application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        }

        invoke_anr_sleep.setOnClickListener {
            Thread.sleep(12000)
        }

        invoke_frame_skip.setOnClickListener {
            Thread.sleep(300)
        }

        Instana.crashReporting?.leave("onCreate")
    }

    override fun onResume() {
        super.onResume()

        Instana.crashReporting?.leave("onResume")
    }
}