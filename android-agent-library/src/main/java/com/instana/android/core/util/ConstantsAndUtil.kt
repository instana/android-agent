package com.instana.android.core.util

import android.app.Application
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RestrictTo
import com.instana.android.Instana
import com.instana.android.instrumentation.InstrumentationType
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


@RestrictTo(RestrictTo.Scope.LIBRARY)
object ConstantsAndUtil {

    const val EMPTY_STR = ""
    const val CELLULAR = "cellular"
    const val WIFI = "wifi"

    const val TYPE_SUCCESS = "success"
    const val TYPE_ERROR = "error"
    const val OS_TYPE = "android"

    const val TRACKING_HEADER_KEY = "X-INSTANA-T"

    val runtime: Runtime by lazy {
        Runtime.getRuntime()
    }

    val client: OkHttpClient by lazy {
        OkHttpClient()
    }

    fun getConnectionType(cm: ConnectivityManager): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            if (capabilities != null) {
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> CELLULAR
                    else -> null
                }
            }
        } else {
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                return when {
                    activeNetwork.type == ConnectivityManager.TYPE_WIFI -> WIFI
                    activeNetwork.type == ConnectivityManager.TYPE_MOBILE -> CELLULAR
                    else -> null
                }
            }
        }
        return null
    }

    fun getCellularConnectionType(tm: TelephonyManager): String = when (tm.networkType) {
        TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
        TelephonyManager.NETWORK_TYPE_LTE -> "4G"
        else -> "Unknown"
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

    @JvmStatic
    fun hasTrackingHeader(header: String?): Boolean = header != null

    @JvmStatic
    fun checkTag(header: String?): Boolean = if (header != null) {
        if (Instana.remoteCallInstrumentation != null) {
            Instana.remoteCallInstrumentation!!.hasTag(header)
        } else {
            false
        }
    } else {
        false
    }

    @JvmStatic
    fun isNotLibraryCallBoolean(url: String?): Boolean = if (url == null) {
        false
    } else {
        !url.contains(Instana.configuration.reportingUrl)
    }

    @JvmStatic
    val isAutoEnabled: Boolean
        get() =
            Instana.configuration.remoteCallInstrumentationType != InstrumentationType.DISABLED.type &&
                    Instana.configuration.remoteCallInstrumentationType != InstrumentationType.MANUAL.type

    fun isDeviceRooted(): Boolean = checkRootMethod1() || checkRootMethod2() || checkRootMethod3()

    private fun checkRootMethod1(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su", "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su")
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = runtime.exec(arrayOf("/system/xbin/which", "su"))
            val br = BufferedReader(InputStreamReader(process!!.inputStream))
            br.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }
}