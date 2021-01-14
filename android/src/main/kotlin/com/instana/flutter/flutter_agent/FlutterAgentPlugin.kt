package com.instana.flutter.flutter_agent

import android.app.Application
import androidx.annotation.NonNull
import com.instana.android.CustomEvent

import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.instrumentation.HTTPMarker
import com.instana.android.instrumentation.HTTPMarkerData

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

/** FlutterAgentPlugin */
class FlutterAgentPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel

    private lateinit var app: Application

    private val markerInstanceMap = mutableMapOf<String, HTTPMarker?>()
    private val markerMethodMap = mutableMapOf<String, String>()

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
            "getView" -> {
                result.success(Instana.view)
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
            "setIgnore" -> {
                val urls: List<String?>? = call.argument("urls")
                if (urls == null) result.error("missingUrlList", "Instana requires non-null 'urls' list", null)
                else {
                    val regex = urls.mapNotNull { it?.toPattern(Pattern.LITERAL) }
                    Instana.ignoreURLs.addAll(regex)
                    result.success(null)
                }
            }
            "setIgnoreRegex" -> {
                val regexStr: List<String?>? = call.argument("regex")
                if (regexStr == null) result.error("missingRegexList", "Instana requires non-null 'regex' list", null)
                else {
                    val regex = regexStr.mapNotNull { it?.toPattern() }
                    Instana.ignoreURLs.addAll(regex)
                    result.success(null)
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
                        this.meta = meta?.filter { it.key != null && it.value != null } as? HashMap<String, String>
                    }
                    Instana.reportEvent(event)
                    result.success(null)
                }
            }
            "startCapture" -> {
                val url: String? = call.argument("url")
                val method: String? = call.argument("method")
                val viewName: String? = call.argument("viewName")
                if (url.isNullOrBlank()) result.error("missingUrl", "Instana requires non-blank 'url'", null)
                else if (method.isNullOrBlank()) result.error("missingMethod", "Instana requires non-blank 'method'", null)
                else {
                    val marker = Instana.startCapture(url, viewName)
                    val markerId = UUID.randomUUID().toString()
                    markerInstanceMap[markerId] = marker
                    markerMethodMap[markerId] = method // TODO remove this map once the native client can receive 'method' in 'startCapture'
                    result.success(markerId)
                }

            }
            "finishCapture" -> {
                val markerId: String? = call.argument("id")
                if (markerId.isNullOrBlank()) result.error("missingMarkerId", "Instana requires non-blank 'markerId'", null)
                else {
                    markerInstanceMap[markerId]?.finish(HTTPMarkerData(
                            requestMethod = markerMethodMap[markerId],
                            responseStatusCode = call.argument("responseStatusCode"),
                            responseSizeEncodedBytes = (call.argument("responseSizeBody") as? Int)?.toLong(),
                            responseSizeDecodedBytes = (call.argument("responseSizeBodyDecoded") as? Int)?.toLong(),
                            backendTraceId = call.argument("backendTracingID"),
                            errorMessage = call.argument("errorMessage")
                    ))
                    markerInstanceMap.remove(markerId)
                    markerMethodMap.remove(markerId)
                    result.success(null)
                }
            }
            "cancelCapture" -> {
                val markerId: String? = call.argument("id")
                if (markerId.isNullOrBlank()) result.error("missingMarkerId", "Instana requires non-blank 'markerId'", null)
                else {
                    markerInstanceMap[markerId]?.cancel()
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
