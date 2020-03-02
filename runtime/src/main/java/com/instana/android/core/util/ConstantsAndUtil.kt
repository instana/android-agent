package com.instana.android.core.util

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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.instana.android.Instana
import com.instana.android.core.event.models.ConnectionType
import com.instana.android.core.event.models.EffectiveConnectionType
import com.instana.android.instrumentation.HTTPCaptureConfig
import okhttp3.OkHttpClient


@RestrictTo(RestrictTo.Scope.LIBRARY)
object ConstantsAndUtil {

    const val EMPTY_STR = ""

    const val OS_TYPE = "android"

    const val TRACKING_HEADER_KEY = "X-INSTANA-T"

    val runtime: Runtime by lazy {
        Runtime.getRuntime()
    }

    val client: OkHttpClient by lazy {
        OkHttpClient()
    }

    @Suppress("DEPRECATION")
    fun getConnectionType(cm: ConnectivityManager): ConnectionType? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            if (capabilities != null) {
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.WIRED
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                    else -> null
                }
            }
        } else {
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                return when (activeNetwork.type) {
                    ConnectivityManager.TYPE_ETHERNET -> ConnectionType.WIRED
                    ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> ConnectionType.CELLULAR
                    else -> null
                }
            }
        }
        return null
    }

    fun getCarrierName(cm: ConnectivityManager, tm: TelephonyManager): String? =
        when (getConnectionType(cm)) {
            ConnectionType.CELLULAR -> tm.networkOperatorName
            else -> null
        }

    fun getCellularConnectionType(cm: ConnectivityManager, tm: TelephonyManager): EffectiveConnectionType? {
        if (getConnectionType(cm) != ConnectionType.CELLULAR) {
            return null
        } else {
            return when (tm.networkType) {
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
    }

    fun getAppVersionNameAndVersionCode(app: Application): Pair<String, String> {
        val versionCode: String?
        val version: String?
        try {
            val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
            version = packageInfo.versionName
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return Pair(EMPTY_STR, EMPTY_STR)
        }
        return Pair(version ?: EMPTY_STR, versionCode)
    }

    fun getViewportWidthAndHeight(app: Application): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        (app.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.getRealMetrics(displayMetrics)
        return displayMetrics.widthPixels to displayMetrics.heightPixels
    }

    @JvmStatic
    fun hasTrackingHeader(header: String?): Boolean = header != null

    @JvmStatic
    fun checkTag(header: String?): Boolean =
        if (header != null) {
            Instana.remoteCallInstrumentation?.hasTag(header) ?: false
        } else {
            false
        }

    @JvmStatic
    fun isNotLibraryCallBoolean(url: String?): Boolean = if (url == null) {
        false
    } else {
        !url.contains(Instana.config.reportingURL)
    }

    @JvmStatic
    val isAutoEnabled: Boolean
        get() = Instana.config.httpCaptureConfig == HTTPCaptureConfig.AUTO

    @JvmStatic
    fun isBlacklistedURL(url: String): Boolean {
        return Instana.ignoreURLs.any {
            it.matches(url) || it.matches(url.removeTrailing("/"))
        }
    }

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
}
