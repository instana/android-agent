/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

import androidx.annotation.IntRange
import androidx.annotation.Size
import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Beacon private constructor(
    type: BeaconType,
    duration: Int,
    mobileAppId: String,
    sessionId: String,
    errorCount: Long,
    appProfile: AppProfile,
    deviceProfile: DeviceProfile
) {

    private val intMap: MutableMap<String, Int> = mutableMapOf()
    private val longMap: MutableMap<String, Long> = mutableMapOf()
    private val stringMap: MutableMap<String, String> = mutableMapOf()
    private val booleanMap: MutableMap<String, Boolean> = mutableMapOf()

    init {
        setAppVersion(appProfile.appVersion)
        setAppBuild(appProfile.appBuild)

        setPlatform(deviceProfile.platform)
        setOsVersion(deviceProfile.osVersion)
        setDeviceManufacturer(deviceProfile.deviceManufacturer)
        setDeviceModel(deviceProfile.deviceModel)
        setRooted(deviceProfile.rooted)
        setViewportWidth(deviceProfile.viewportWidth)
        setViewportHeight(deviceProfile.viewportHeight)

        setTimestamp(System.currentTimeMillis())
        setBeaconId(UUID.randomUUID().toString())

        setMobileAppId(mobileAppId)
        setSessionId(sessionId)
        setType(type)
        setBatchSize(1) // TODO isn't this optional except for crashes?
        setDuration(duration.toLong())
        setErrorCount(errorCount)
    }

    /**
     * The version of our iOS/Android agents/data collectors. This information is useful to identify which customers
     * are still relying on old/outdated agents.
     */
    fun setAgentVersion(@Size(max = 128) value: String) {
        stringMap["av"] = value
    }

    /**
     * This is the ID under which data can be reported to Instana. This ID will be created when creating a mobile app via
     * the UI.
     */
    fun setMobileAppId(@Size(min = 1, max = 64) value: String) {
        stringMap["k"] = value
    }

    /**
     * The timestamp in ms when the beacon has been created
     */
    fun setTimestamp(@IntRange(from = 1) value: Long) {
        longMap["ti"] = value
    }

    /**
     * In case of instantaneous events, use 0.
     */
    fun setDuration(@IntRange(from = 0) value: Long) {
        longMap["d"] = value
    }

    /**
     * Errors may be batched within the browser similar to how exit spans can be batched.
     *
     * As of 2018-10-23 this batching is only done for beacons of type [BeaconType.CRASH].
     */
    fun setBatchSize(@IntRange(from = 1) value: Long) {
        longMap["bs"] = value
    }

    /**
     * Session ID (UUID) will be created after each app launch.
     * Each Session ID has a timeout of XY seconds.
     * The Session ID must not be empty.
     */
    fun setSessionId(@Size(min = 1, max = 128) value: String) {
        stringMap["sid"] = value
    }

    /**
     * An unique UUID for each beaon.
     */
    fun setBeaconId(@Size(min = 1, max = 128) value: String) {
        stringMap["bid"] = value
    }

    /**
     * The backend exposes trace IDs via the Server-Timing HTTP response header.
     * The app needs to pick up the trace ID from this header and put it into this field.
     * For example: Server-Timing: intid;desc=bd777df70e5e5356
     * In this case the field should hold the value bd777df70e5e5356.
     * This allows us to build a connection between end-user (mobile monitoring) and backend activity (tracing).
     */
    fun setBackendTraceId(@Size(max = 128) value: String) {
        stringMap["bt"] = value
    }

    /**
     * The type of the beacon.
     */
    fun setType(value: BeaconType) {
        stringMap["t"] = value.internalType
    }

    /**
     * The kind of view/page/screen the user is on, e.g. login, checkout…
     */
    fun setView(@Size(max = 256) value: String) {
        stringMap["v"] = value
    }

    /**
     * Defines what kind of event has happened on your website that should result in the transmission of a custom beacon.
     */
    fun setCustomEventName(@Size(max = 256) value: String) {
        stringMap["cen"] = value
    }

    /**
     * Custom key/value pairs that users can define within their App.
     *
     * To be transmitted to be backend as multiple m_ prefixed key value pairs. For example:
     *
     * m_user  Tom Mason
     * m_email tom.mason@example.com
     */
    fun setMeta(@Size(max = 64) key: String, @Size(max = 64) value: String) {
        stringMap["m_$key"] = value
    }

    /**
     * An identifier for the user. (optional)
     */
    fun setUserId(@Size(max = 128) value: String) {
        stringMap["ui"] = value
    }

    /**
     * The user name. (optional)
     */
    fun setUserName(@Size(max = 128) value: String) {
        stringMap["un"] = value
    }

    /**
     * The user’s email address. (optional)
     */
    fun setUserEmail(@Size(max = 128) value: String) {
        stringMap["ue"] = value
    }

    // TODO check with Ben to see how this list is serialized
//    /**
//     * The current selected language for the app
//     * The language is described using BCP 47 language tags.
//     * <p>
//     * For example: en-US
//
//          short serialization key: "ul"
//     */
//    @Nullable
//    @Size(max = 5)
//    public List<String> userLanguages;

    /**
     * The bundle identifier uniquely identifies an app. Two apps cannot have the same bundle identifier.
     * To avoid conflicts, developers should use reverse domain name notation for choosing an app's bundle identifier
     * (e.g. com.instana.demoapp.android). The bundle identifier can contain a suffix for the staging environment
     * (e.g. .test or .dev).
     */
    fun setBundleIdentifier(@Size(max = 128) value: String) {
        stringMap["bi"] = value
    }

    /**
     * AppBuild specifies the build version number of the bundle, which identifies an iteration (released or unreleased) of the bundle
     * The AppBuild is unique for each AppVersion and should be incremented with each deployed build.
     *
     * For example: 1203
     */
    fun setAppBuild(@Size(max = 128) value: String) {
        stringMap["ab"] = value
    }

    /**
     * AppVersion specifies the version for each store release. The AppVersion should conform to the semantic versioning.
     *
     * For example: 1.3.1
     */
    fun setAppVersion(@Size(max = 128) value: String) {
        stringMap["av"] = value
    }

    /**
     * The abstract platform the app is running on. Ignores differences / modifications made by vendors. To encode
     * these differences, please use [.setOsName], [.setOsVersion] and the various device fields.
     *
     * @see Platform
     */
    fun setPlatform(value: Platform) {
        stringMap["p"] = value.internalType
    }

    /**
     * The name of the operating system as provided by the runtime.
     */
    fun setOsName(@Size(max = 128) value: String) {
        stringMap["osn"] = value
    }

    /**
     * The OS version of the platform without any information about OS name itself.
     *
     * For example: 12.0.1
     */
    fun setOsVersion(@Size(max = 128) value: String) {
        stringMap["osv"] = value
    }

    /**
     * For example: Apple
     */
    fun setDeviceManufacturer(@Size(max = 128) value: String) {
        stringMap["dma"] = value
    }

    /**
     * For example: iPhone 6 XS
     */
    fun setDeviceModel(@Size(max = 128) value: String) {
        stringMap["dmo"] = value
    }

    /**
     * For example: MP1.0
     */
    fun setDeviceHardware(@Size(max = 128) value: String) {
        stringMap["dh"] = value
    }

    /**
     * Whether the mobile device is rooted / jailbroken. True indicates that the device is definitely rooted / jailbroken.
     * False indicates that it isn't or that we could not identify the correct it.
     */
    fun setRooted(value: Boolean) {
        booleanMap["ro"] = value
    }

    /**
     * Whether the mobile device has the Google Play Services installed. Not having these installed can denote a
     * source of errors. True indicates that the platform is [Platform.ANDROID] and that the
     * Google Play Services are definitely missing. False indicates that it isn't applicable or that they are installed.
     */
    fun setGooglePlayServicesMissing(value: Boolean) {
        booleanMap["gpsm"] = value
    }

    /**
     * Device screen width in pixels
     *
     * For example: 2436
     *
     * -1 means that the value wasn't recorded.
     */
    fun setViewportWidth(@IntRange(from = 1) value: Int) {
        intMap["vw"] = value
    }

    /**
     * Device screen height in pixels
     *
     * For example: 1125
     *
     * -1 means that the value wasn't recorded.
     */
    fun setViewportHeight(@IntRange(from = 1) value: Int) {
        intMap["vh"] = value
    }

    /**
     * The cellular carrier name
     *
     * For example: Deutsche Telekom, Sprint, Verizon
     */
    fun setCarrier(@Size(max = 256) value: String) {
        stringMap["cn"] = value
    }

    /**
     * The connection type
     *
     * @see ConnectionType
     */
    fun setConnectionType(value: ConnectionType) {
        stringMap["ct"] = value.internalType
    }

    /**
     * https://wicg.github.io/netinfo/#dom-effectiveconnectiontype
     */
    fun setEffectiveConnectionType(value: EffectiveConnectionType) {
        stringMap["ect"] = value.internalType
    }

    /**
     * The full URL for HTTP calls of all kinds.
     *
     * For example: https://stackoverflow.com/questions/4604486/how-do-i-move-an-existing-git-submodule-within-a-git-repository
     */
    fun setHttpCallUrl(@Size(max = 4096) value: String) {
        stringMap["hu"] = value
    }

    /**
     * The request's http method.
     *
     * For example: POST
     */
    fun setHttpCallMethod(@Size(max = 16) value: String) {
        stringMap["hm"] = value
    }

    /**
     * HTTP status code
     * Zero means that the value wasn't recorded.
     *
     * For example: 404
     */
    fun setHttpCallStatus(@IntRange(from = -1, to = 599) value: Int) {
        intMap["hs"] = value
    }

    /**
     * Only available for [BeaconType.HTTP_REQUEST]. Indicates the size of the encoded
     * (e.g. zipped) HTTP response body. Does not include the size of headers. Can be equal to [.setDecodedBodySize]
     * when the response is not compressed.
     *
     * -1 means that the value wasn't recorded.
     */
    fun setEncodedBodySize(@IntRange(from = -1) value: Int) {
        intMap["ebs"] = value
    }

    /**
     * Only available for [BeaconType.HTTP_REQUEST]. Indicates the size of the decoded
     * (e.g. unzipped) HTTP response body. Does not include the size of headers. Can be equal to [.setEncodedBodySize]
     * when the response is not compressed.
     *
     * -1 means that the value wasn't recorded.
     */
    fun setDecodedBodySize(@IntRange(from = -1) value: Int) {
        intMap["dbs"] = value
    }

    /**
     * Only available for [BeaconType.HTTP_REQUEST]. Indicates the total size of the HTTP
     * response including response headers and the encoded response body.
     *
     * -1 means that the value wasn't recorded.
     */
    fun setTransferSize(@IntRange(from = -1) value: Int) {
        intMap["trs"] = value
    }

    /**
     * "errorCount must be <= batchSize"
     */
    fun setErrorCount(@IntRange(from = 0) value: Long) {
        longMap["ec"] = value
    }

    /**
     * An arbitrary error message sent by the app.
     *
     * For example: "Error: Could not start a payment request."
     */
    fun setErrorMessage(@Size(max = 16384) value: String) {
        stringMap["em"] = value
        // TODO verify if md5 is good here
        // TODO decide what to do when the md5 is null (use value.hashCode()?)
        setErrorId(value.md5())
    }

    /**
     * error ID must be defined when there is an error message
     */
    private fun setErrorId(@Size(max = 16384) value: String) {
        stringMap["ei"] = value
    }

    /**
     * Type of the error
     * For iOS: You could use the ErrorDomain or the Swift Error enum case
     * For example: "NetworkError.timeout"
     */
    fun setErrorType(@Size(max = 1024) value: String) {
        stringMap["et"] = value
    }

    /**
     * A stacktrace of active threads.
     */
    fun setStackTrace(@Size(max = 16384) value: String) {
        stringMap["st"] = value
    }

    @Suppress("DuplicatedCode") // I rather duplicate a few lines of code and keep type safety
    override fun toString(): String {
        val sb = StringBuilder()
        booleanMap.forEach {
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        intMap.forEach {
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        longMap.forEach {
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        stringMap.forEach {
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        return sb.toString()
    }

    @VisibleForTesting
    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    companion object {
        fun newSessionStart(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile
        ): Beacon {
            return Beacon(BeaconType.SESSION_START, 0, appKey, UUID.randomUUID().toString(), 0, appProfile, deviceProfile)
        }

        fun newHttpRequest(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            sessionId: String,
            duration: Int,
            method: String,
            url: String,
            responseCode: Int,
            error: String,
            carrier: String,
            connectionType: ConnectionType,
            requestSizeBytes: Long, //TODO ignored?
            responseSizeBytes: Int
        ): Beacon { // must set customEventName
            return Beacon(BeaconType.HTTP_REQUEST, duration, appKey, sessionId, 0, appProfile, deviceProfile)
                .apply {
                    setHttpCallMethod(method)
                    setHttpCallUrl(url)
                    setHttpCallStatus(responseCode)
                    setErrorMessage(error)
                    setCarrier(carrier)
                    setConnectionType(connectionType)
                    setDecodedBodySize(responseSizeBytes)
                }
        }

        fun newViewChange(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            sessionId: String,
            view: String
        ): Beacon {
            return Beacon(BeaconType.VIEW_CHANGE, 0, appKey, sessionId, 0, appProfile, deviceProfile)
                .apply {
                    setView(view)
                }
        }

        fun newCustomEvent(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            sessionId: String,
            duration: Int,
            name: String,
            meta: Map<String, String>
        ): Beacon {
            return Beacon(BeaconType.CUSTOM, duration, appKey, sessionId, 0, appProfile, deviceProfile)
                .apply {
                    setCustomEventName(name)
                    meta.forEach { setMeta(it.key, it.value) }
                }
        }

        fun newCrash(
            sessionId: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile
        ): Beacon { // might need batchSize. If not needed, remove batchSize
            return Beacon(BeaconType.CRASH, 0, "", sessionId, 0, appProfile, deviceProfile)
        }
    }
}
