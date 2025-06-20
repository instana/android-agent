/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import android.Manifest
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RestrictTo
import androidx.core.app.ActivityCompat
import com.instana.android.Instana
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.event.models.ConnectionType
import com.instana.android.core.event.models.EffectiveConnectionType
import com.instana.android.core.event.models.Platform
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.performance.appstate.AppState
import com.instana.android.view.ScreenAttributes
import okhttp3.OkHttpClient
import java.io.PrintWriter
import java.io.StringWriter
import java.net.MalformedURLException
import java.net.URL


@RestrictTo(RestrictTo.Scope.LIBRARY)
object ConstantsAndUtil {

    const val EMPTY_STR = ""

    const val TRACKING_HEADER_KEY = "X-INSTANA-ANDROID"

    const val TRACE_PARENT = "traceparent"

    const val TRACE_STATE = "tracestate"

    val runtime: Runtime by lazy {
        Runtime.getRuntime()
    }

    val client: OkHttpClient by lazy {
        var client: OkHttpClient? = null
        if (Instana.config?.debugTrustInsecureReportingURL == true) {
            Logger.w("debugTrustInsecureReportingURL is on, this option allows instana to report data even for server connections otherwise considered insecure.")
            val (insecureSocketFactory, insecureTrustAllManager) = TLSSocketFactory.newInsecureSocketFactory()
            client = OkHttpClient.Builder()
                .sslSocketFactory(insecureSocketFactory, insecureTrustAllManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } else if (Build.VERSION.SDK_INT < 20) {
            // Enable TLSv1.2 support: https://developer.android.com/reference/javax/net/ssl/SSLSocket.html#protocols
            val trustManager = TLSSocketFactory.getTrustManagers()
            if (trustManager != null) {
                client = OkHttpClient.Builder()
                    .sslSocketFactory(TLSSocketFactory(), trustManager)
                    .build()
            }
        }
        client ?: OkHttpClient()
    }

    fun getOsName(): String {
        return if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.BASE_OS.isNotBlank()) {
            Build.VERSION.BASE_OS
        } else {
            Platform.ANDROID.internalType
        }
    }

    fun getConnectionType(context: Context, cm: ConnectivityManager): ConnectionType? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            Logger.w("Missing permission 'ACCESS_NETWORK_STATE'. Instana Agent won't be able to detect connection type")
            return null
        }

        if (Build.VERSION.SDK_INT >= 23) {
            val network = cm.activeNetwork
            try { // TODO remove when https://issuetracker.google.com/issues/175055271 is solved
                val capabilities = cm.getNetworkCapabilities(network)
                if (capabilities != null) {
                    return when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                        else -> null
                    }
                }
            } catch (e: SecurityException) {
                Logger.w("Failed to detect connection type", e)
            }
        } else {
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                return when (activeNetwork.type) {
                    ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                    ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> ConnectionType.CELLULAR
                    else -> null
                }
            }
        }
        return null
    }

    fun getConnectionProfile(context: Context): ConnectionProfile {
        val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val tm: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return ConnectionProfile(
            carrierName = getCarrierName(context, cm, tm),
            connectionType = getConnectionType(context, cm),
            effectiveConnectionType = getCellularConnectionType(context, cm, tm)
        )
    }
    fun getCarrierName(context: Context, cm: ConnectivityManager, tm: TelephonyManager): String? =
        when (getConnectionType(context, cm)) {
            ConnectionType.CELLULAR -> tm.networkOperatorName
            else -> null
        }

    fun getCellularConnectionType(context: Context, cm: ConnectivityManager, tm: TelephonyManager): EffectiveConnectionType? {
        if (getConnectionType(context, cm) != ConnectionType.CELLULAR) {
            return null
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Logger.w("Missing permission 'READ_PHONE_STATE'. Instana Agent won't be able to detect cellular network type")
            return null
        }
        val networkType = if (Build.VERSION.SDK_INT >= 24) {
            tm.dataNetworkType
        } else {
            @Suppress("DEPRECATION")
            tm.networkType
        }
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN,
                -> EffectiveConnectionType.TYPE_2G

            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP,
                -> EffectiveConnectionType.TYPE_3G

            TelephonyManager.NETWORK_TYPE_LTE -> EffectiveConnectionType.TYPE_4G
            else -> null
        }
    }

    fun getAppVersionNameAndVersionCode(app: Application): Pair<String, String> {
        val versionCode: String?
        val version: String?
        try {
            val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
            version = packageInfo.versionName
            versionCode = if (Build.VERSION.SDK_INT >= 28) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("Failed to detect app versionName and versionCode", e)
            return Pair(EMPTY_STR, EMPTY_STR)
        }
        return Pair(version ?: EMPTY_STR, versionCode)
    }

    fun hasTrackingHeader(header: String?): Boolean = header != null

    fun checkTag(header: String?): Boolean =
        if (header != null) {
            Instana.instrumentationService?.hasTag(header) ?: false
        } else {
            false
        }

    fun getViewportWidthAndHeight(app: Application): Pair<Int?, Int?> {
        val display = (app.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
        return if (Build.VERSION.SDK_INT >= 17) {
            val displayMetrics = DisplayMetrics()
            display?.getRealMetrics(displayMetrics)
            displayMetrics.widthPixels to displayMetrics.heightPixels
        } else {
            @Suppress("DEPRECATION")
            display?.width to display?.height
        }
    }

    fun isLibraryCallBoolean(url: String?): Boolean =
        url?.let {
            it.contains(Instana.config?.reportingURLWithPort ?: "") ||
                    it.contains(Instana.config?.reportingURLWithoutPort ?: "")
        } ?: true

    val isAutoEnabled: Boolean
        get() = Instana.config?.httpCaptureConfig == HTTPCaptureConfig.AUTO

    fun isBlacklistedURL(url: String): Boolean {
        val ignoreURLs = Instana.ignoreURLs
        return Instana.internalURLs.any { it.matches(url) } ||
                synchronized(ignoreURLs) {
                    ignoreURLs.map { it.toRegex() }.any {
                        it.matches(url) || it.matches(url.removeTrailing("/"))
                    }
                }
    }

    private fun isHeaderToCapture(header: String): Boolean {
        val headers = Instana.captureHeaders
        synchronized(headers) {
            return headers.any { it.toRegex().matches(header) }
        }
    }

    internal fun getCapturedRequestHeaders(headers: Map<String, String>): Map<String, String> {
        @Suppress("UNCHECKED_CAST")
        return try {
            headers.keys
                .filter { headerName -> isHeaderToCapture(headerName) }
                .associateWith { headerName -> headers[headerName] }
                .filterValues { value -> value != null } as Map<String, String>
        } catch (e: Exception) {
            //Instana agent crashes shouldn't affect app performance.
            Logger.i("Exception at getCapturedResponseHeaders: ${e.localizedMessage}")
            emptyMap()
        }

    }

    internal fun getCapturedResponseHeaders(headers: Map<String, String>): Map<String, String> {
        @Suppress("UNCHECKED_CAST")
        return try {
            headers.keys
                .filter { headerName -> isHeaderToCapture(headerName) }
                .associateWith { headerName -> headers[headerName] }
                .filterValues { value -> value != null } as Map<String, String>
        } catch (e: Exception) {
            //Instana agent crashes shouldn't affect app performance.
            Logger.i("Exception at getCapturedResponseHeaders: ${e.localizedMessage}")
            emptyMap()
        }

    }

    fun redactQueryParams(url: String): String {
        val redactHTTPQuery = Instana.redactHTTPQuery
        synchronized(redactHTTPQuery) {
            val regexList =
                if (redactHTTPQuery.size > 0) redactHTTPQuery.map { it.toRegex() }
                else Instana.config?.defaultRedactedQueryParams ?: emptyList()

            return URLUtils.redactURLQueryParams(
                url = url,
                replacement = Instana.config?.defaultRedactedQueryParamValue ?: "",
                regex = regexList
            )
        }
    }

    internal fun forceRedundantURLPort(url: String): String {
        return try {
            val originalURL = URL(url)
            val newPort = if (originalURL.port != -1) originalURL.port else originalURL.defaultPort
            URL(originalURL.protocol, originalURL.host, newPort, originalURL.file).toString()
        } catch (e: MalformedURLException) {
            Logger.w("URL seems malformed: $url")
            Logger.w(e.toString())
            url
        }
    }

    internal fun forceNoRedundantURLPort(url: String): String {
        return try {
            val originalURL = URL(url)
            val isPortRedundant = originalURL.port == originalURL.defaultPort
            if (isPortRedundant) {
                URL(originalURL.protocol, originalURL.host, originalURL.file).toString()
            } else {
                url
            }
        } catch (e: MalformedURLException) {
            Logger.w("URL seems malformed: $url")
            Logger.w(e.toString())
            url
        }
    }

    internal fun Map<String, String>.validateAllKeys(): Map<String, String> {
        val listOfValidKeysAndroid = ScreenAttributes.values().map { it.value }.toSet()
        val listOfValidKeysFlutter = setOf(
            "settings.route.name",
            "widget.name",
            "child.widget.name",
            "child.widget.title",
            "go.router.path"
        )
        return if (this.keys.all { key -> key in listOfValidKeysAndroid || key in listOfValidKeysFlutter }) {
            this
        } else {
            emptyMap()
        }
    }

    fun Int.toDaysInMillis(): Long {
        return maxOf(this, 0) * 24 * 60 * 60 * 1000L
    }

    internal fun mapToJsonString(map: Map<String, String>): String {
        return map.entries.joinToString(prefix = "{", postfix = "}", separator = ", ") {
            "\"${it.key}\": \"${it.value}\""
        }
    }

    internal fun dumpAllThreads(crashedThread: Thread?, throwable: Throwable?): String {
        val stackTraces = Thread.getAllStackTraces()

        if (!stackTraces.containsKey(crashedThread)) {
            stackTraces[crashedThread] = crashedThread?.stackTrace
        }

        if (throwable != null) { // unhandled errors use the exception trace
            stackTraces[crashedThread] = throwable.stackTrace
        }

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        for ((t, u) in stackTraces) {
            ThreadUtil.println(pw, t, u)
        }
        pw.flush()
        return sw.toString()
    }

    internal fun isAppInForeground(context: Context?): AppState {
        return try {
            val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            val runningAppProcesses = activityManager?.runningAppProcesses ?: return AppState.BACKGROUND
            val myProcess = runningAppProcesses?.firstOrNull { it.pid == android.os.Process.myPid() }
            val isForeground = myProcess?.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            if (isForeground) {
                AppState.FOREGROUND
            } else {
                AppState.BACKGROUND
            }
        } catch (e: Exception) {
            Logger.i("App State not identified! : ${e.localizedMessage}")
            AppState.UN_IDENTIFIED
        }
    }

    /**
     * The following code generate W3C trace context headers, ensuring compatibility with OpenTelemetry (OTel).
     * The "03" flag at the end indicates that the trace was randomly generated and is not sampled from the frontend.
     * If the trace generation method changes in the future, remove the "03" flag from the end.
     * The format is made similar to the weasel implementation
     * References:
     * https://www.w3.org/TR/trace-context-2/#trace-flags
     * https://www.w3.org/TR/trace-context-2/#random-trace-id-flag
     * Instana Trace header support details:
     * https://github.ibm.com/instana/technical-documentation/tree/master/tracing/specification#incoming-correlation-part-plus-w3c-trace-context-specification-headers
     */
    internal fun generateW3CHeaders(): Map<String, String> {
        val traceState = UniqueIdManager.generateUniqueIdImpl()
        val traceParent = "00-0000000000000000$traceState-$traceState-03"
        return mapOf(
            TRACE_PARENT to traceParent,
            TRACE_STATE to traceState
        )
    }

    internal fun bytesToMbConvertor(bytes: Long): Long {
        return bytes / (1024 * 1024)
    }

    internal fun isBackgroundEnuEnabled() =
        (Instana.config?.trustDeviceTiming == true && Instana.config?.performanceMonitorConfig?.enableBackgroundEnuReport == true)
}
