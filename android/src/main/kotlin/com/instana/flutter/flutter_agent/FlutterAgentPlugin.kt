package com.instana.flutter.flutter_agent

import android.app.Application
import androidx.annotation.NonNull
import com.instana.android.CustomEvent

import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.instrumentation.HTTPCaptureConfig

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterAgentPlugin */
class FlutterAgentPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var app: Application

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_agent")
        channel.setMethodCallHandler(this)

        // TODO applicationContext might be null. Need to store as I did with RN
        app = flutterPluginBinding.applicationContext as Application
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "setup" -> {
                val key: String? = call.argument("key")
                val reportingUrl: String? = call.argument("reportingUrl")
                if (key.isNullOrBlank()) result.error("missingAppKey", "Instana set up requires a non-blank 'key'", null)
                else if (reportingUrl.isNullOrBlank()) result.error("missingReportingUrl", "Instana set up requires a non-blank 'reportingUrl'", null)
                else {
                    Instana.setup(
                            app,
                            InstanaConfig(
                                    reportingURL = reportingUrl,
                                    key = key,
                                    httpCaptureConfig = HTTPCaptureConfig.MANUAL
                            )
                    )
                    result.success(null)
                }
            }
            "setUserID" -> {
                val userID: String? = call.argument("userID")
                Instana.userId = userID
                result.success(null)
            }
            "setUserName" -> {
                val userName: String? = call.argument("userName")
                Instana.userName = userName
                result.success(null)
            }
            "setUserEmail" -> {
                val userEmail: String? = call.argument("userEmail")
                Instana.userEmail = userEmail
                result.success(null)
            }
            "setView" -> {
                val viewName: String? = call.argument("viewName")
                Instana.view = viewName
                result.success(null)
            }
            "setMeta" -> {
                val key: String? = call.argument("key")
                val value: String? = call.argument("value")
                if (key.isNullOrBlank()) result.error("missingMetaKey", "Instana requires non-blank 'meta keys'", null)
                else if (value == null) result.error("missingMetaValue", "Instana requires non-null 'meta values'", null)
                else {
                    val putSuccess = Instana.meta.put(key, value)
                    if (putSuccess) result.success(null)
                    else result.error("errorAddingMeta", "Instana failed to add new meta value", null)
                }
            }
            "reportEvent" -> {
                val eventName: String? = call.argument("eventName")
                val startTime: Double? = call.argument("startTime")
                val duration: Double? = call.argument("duration")
                val viewName: String? = call.argument("viewName")
                val meta: HashMap<String?, String?>? = call.argument("meta")
                val backendTracingID: String? = call.argument("backendTracingID")
                if (eventName.isNullOrBlank()) result.error("missingEventName", "Instana requires non-blank 'event name'", null)
                else {
                    val event = CustomEvent(eventName).apply {
                        this.startTime = startTime?.toLong()
                        this.duration = duration?.toLong()
                        this.viewName = viewName
                        this.backendTracingID = backendTracingID
                        this.meta = meta?.filter { it.key != null && it.value != null} as? HashMap<String,String>
                    }
                    Instana.reportEvent(event)
                    result.success(null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
