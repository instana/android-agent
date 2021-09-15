/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import android.Manifest
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
import com.instana.android.core.event.models.ConnectionType
import com.instana.android.core.event.models.EffectiveConnectionType
import com.instana.android.core.event.models.Platform
import com.instana.android.instrumentation.HTTPCaptureConfig
import okhttp3.OkHttpClient
import java.net.MalformedURLException
import java.net.URL


@RestrictTo(RestrictTo.Scope.LIBRARY)
object ConstantsAndUtil {

    const val EMPTY_STR = ""

    const val TRACKING_HEADER_KEY = "X-INSTANA-ANDROID"

    val runtime: Runtime by lazy {
        Runtime.getRuntime()
    }

    val client: OkHttpClient by lazy {
        var client: OkHttpClient? = null
        if (Build.VERSION.SDK_INT < 20) {
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
            TelephonyManager.NETWORK_TYPE_IDEN -> EffectiveConnectionType.TYPE_2G
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP -> EffectiveConnectionType.TYPE_3G
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

    @JvmStatic
    fun hasTrackingHeader(header: String?): Boolean = header != null

    @JvmStatic
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

    @JvmStatic
    fun isLibraryCallBoolean(url: String?): Boolean =
        url?.let {
            it.contains(Instana.config?.reportingURLWithPort ?: "") ||
                    it.contains(Instana.config?.reportingURLWithoutPort ?: "")
        } ?: true

    @JvmStatic
    val isAutoEnabled: Boolean
        get() = Instana.config?.httpCaptureConfig == HTTPCaptureConfig.AUTO

    @JvmStatic
    val isCollectionEnabled: Boolean
        get() = Instana.config?.collectionEnabled == true

    @JvmStatic
    fun isBlacklistedURL(url: String): Boolean {
        return Instana.internalURLs.any { it.matches(url) } ||
                Instana.ignoreURLs.map { it.toRegex() }.any {
                    it.matches(url) || it.matches(url.removeTrailing("/"))
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
}
