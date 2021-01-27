/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.flutter.agent

import android.app.Application
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*
import kotlin.collections.HashMap

/** InstanaAgentPlugin */
class InstanaAgentPlugin : FlutterPlugin, MethodCallHandler {

    private var channel: MethodChannel? = null
    private var app: Application? = null

    private val nativeLink = NativeLink()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "instana_agent").apply {
            setMethodCallHandler(this@InstanaAgentPlugin)
        }

        app = flutterPluginBinding.applicationContext as? Application
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "setup" -> {
                val key: String? = call.argument("key")
                val reportingUrl: String? = call.argument("reportingUrl")
                nativeLink.setUpInstana(
                        result = result,
                        app = app,
                        reportingUrl = reportingUrl,
                        key = key)
            }
            "setUserID" -> {
                val userID: String? = call.argument("userID")
                nativeLink.setUserId(
                        result = result,
                        userID = userID)
            }
            "setUserName" -> {
                val userName: String? = call.argument("userName")
                nativeLink.setUserName(
                        result = result,
                        userName = userName)
            }
            "setUserEmail" -> {
                val userEmail: String? = call.argument("userEmail")
                nativeLink.setUserEmail(
                        result = result,
                        userEmail = userEmail)
            }
            "setView" -> {
                val viewName: String? = call.argument("viewName")
                nativeLink.setView(
                        result = result,
                        viewName = viewName)
            }
            "getView" -> {
                result.success(nativeLink.getView())
            }
            "getSessionID" -> {
                result.success(nativeLink.getSessionID())
            }
            "setMeta" -> {
                val key: String? = call.argument("key")
                val value: String? = call.argument("value")
                nativeLink.setMeta(
                        result = result,
                        key = key,
                        value = value)
            }
            "reportEvent" -> {
                val eventName: String? = call.argument("eventName")
                val startTime: Double? = call.argument("startTime")
                val duration: Double? = call.argument("duration")
                val viewName: String? = call.argument("viewName")
                val meta: HashMap<String?, String?>? = call.argument("meta")
                val backendTracingID: String? = call.argument("backendTracingID")
                nativeLink.reportEvent(
                        result = result,
                        eventName = eventName,
                        startTime = startTime,
                        duration = duration,
                        viewName = viewName,
                        meta = meta,
                        backendTracingID = backendTracingID)
            }
            "startCapture" -> {
                val url: String? = call.argument("url")
                val method: String? = call.argument("method")
                val viewName: String? = call.argument("viewName")
                nativeLink.startCapture(
                        result = result,
                        url = url,
                        method = method,
                        viewName = viewName)
            }
            "finish" -> {
                val markerId: String? = call.argument("id")
                val responseStatusCode: Int? = call.argument("responseStatusCode")
                val responseSizeEncodedBytes: Long? = (call.argument("responseSizeBody") as? Int)?.toLong()
                val responseSizeDecodedBytes: Long? = (call.argument("responseSizeBodyDecoded") as? Int)?.toLong()
                val backendTraceId: String? = call.argument("backendTracingID")
                val errorMessage: String? = call.argument("errorMessage")
                nativeLink.finishCapture(
                        result = result,
                        markerId = markerId,
                        responseStatusCode = responseStatusCode,
                        responseSizeEncodedBytes = responseSizeEncodedBytes,
                        responseSizeDecodedBytes = responseSizeDecodedBytes,
                        backendTraceId = backendTraceId,
                        errorMessage = errorMessage)
            }
            "cancel" -> {
                val markerId: String? = call.argument("id")
                nativeLink.cancelCapture(
                        result = result,
                        markerId = markerId)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
    }

}
