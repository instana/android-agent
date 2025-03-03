/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.event.models

import androidx.annotation.IntRange
import androidx.annotation.Size
import androidx.annotation.VisibleForTesting
import com.instana.android.Instana
import com.instana.android.android.agent.BuildConfig
import com.instana.android.core.util.Logger
import com.instana.android.core.util.UniqueIdManager
import com.instana.android.performance.PerformanceMetric
import com.instana.android.performance.PerformanceSubType
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate")
internal class Beacon private constructor(
    type: BeaconType,
    duration: Long,
    mobileAppId: String,
    sessionId: String,
    errorCount: Long,
    appProfile: AppProfile,
    deviceProfile: DeviceProfile,
    connectionProfile: ConnectionProfile,
    userProfile: UserProfile
) {

    private val intMap: MutableMap<String, Int> = mutableMapOf()
    private val longMap: MutableMap<String, Long> = mutableMapOf()
    private val doubleMap: MutableMap<String, Double> = mutableMapOf()
    private val stringMap: MutableMap<String, String> = mutableMapOf()
    private val booleanMap: MutableMap<String, Boolean> = mutableMapOf()

    init {
        // Agent
        setAgentVersion(retrieveVersionName()) //TODO this has a new serialization key. Test when backend is deployed https://instana.slack.com/archives/GQS1KRJ5D/p1582630642005600

        // App
        appProfile.appVersion?.run { setAppVersion(this) }
        appProfile.appBuild?.run { setAppBuild(this) }
        appProfile.appId?.run { setBundleIdentifier(this) }

        // Device
        deviceProfile.platform?.run { setPlatform(this) }
        deviceProfile.osName?.run { setOsName(this) }
        deviceProfile.osVersion?.run { setOsVersion(this) }
        deviceProfile.deviceManufacturer?.run { setDeviceManufacturer(this) }
        deviceProfile.deviceModel?.run { setDeviceModel(this) }
        deviceProfile.deviceHardware?.run { setDeviceHardware(this) }
        deviceProfile.googlePlayServicesMissing?.run { setGooglePlayServicesMissing(this) }
        listOfNotNull(deviceProfile.locale).run { setUserLanguages(this) }
        deviceProfile.rooted?.run { setRooted(this) }
        deviceProfile.viewportWidth?.run { setViewportWidth(this) }
        deviceProfile.viewportHeight?.run { setViewportHeight(this) }

        // Connection
        connectionProfile.carrierName?.run { setCarrier(this) }
        connectionProfile.connectionType?.run { setConnectionType(this) }
        connectionProfile.effectiveConnectionType?.run { setEffectiveConnectionType(this) }

        // User
        userProfile.userId?.run { setUserId(this) }
        userProfile.userName?.run { setUserName(this) }
        userProfile.userEmail?.run { setUserEmail(this) }

        // Beacon - required & autogenerated
        setTimestamp(System.currentTimeMillis())
        setBeaconId(UniqueIdManager.generateUniqueIdImpl())

        // Beacon - required
        setMobileAppId(mobileAppId)
        setSessionId(sessionId)
        setType(type)
        setDuration(duration)
        setErrorCount(errorCount)

        // Setting unique id for user only if usiRefreshTimeIntervalInHrs is non zero
        Instana.config?.takeIf { it.usiRefreshTimeIntervalInHrs != 0L }?.let {
            setUserSessionId(UniqueIdManager.getUniqueId())
        }

        /**
         * Mobile Features - adding active features to beacon, Adding it in init as usesFeature
         * needs to be send with each beacons.
         */
        Instana.config?.run {
            val features = mutableListOf<MobileFeature>()
            if (enableCrashReporting) features += MobileFeature.CRASH
            if (autoCaptureScreenNames) features += MobileFeature.AUTO_CAPTURE_SCREEN_NAME
            if (performanceMonitorConfig.enableAnrReport) features += MobileFeature.ANR
            if (performanceMonitorConfig.enableLowMemoryReport) features += MobileFeature.LOW_MEMORY
            if (dropBeaconReporting) features += MobileFeature.DROP_BEACON
            if (features.isNotEmpty()) setMobileFeatures(features)
        }
    }

    /**
     * The version of our iOS/Android agents/data collectors. This information is useful to identify which customers
     * are still relying on old/outdated agents.
     */
    fun setAgentVersion(@Size(max = 128) value: String) {
        stringMap["agv"] = value.truncate(128, "Agent Version")
    }

    /**
     * This is the ID under which data can be reported to Instana. This ID will be created when creating a mobile app via
     * the UI.
     */
    fun setMobileAppId(@Size(min = 1, max = 64) value: String) {
        stringMap["k"] = value.truncate(64, "Mobile App ID")
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
        stringMap["sid"] = value.truncate(128, "Session ID")
    }

    /**
     * An unique UUID for each beacon.
     */
    fun setBeaconId(@Size(min = 1, max = 16) value: String) {
        stringMap["bid"] = value.truncate(16, "Beacon ID")
    }

    fun getBeaconId(): String? = stringMap["bid"]

    /**
     * The backend exposes trace IDs via the Server-Timing HTTP response header.
     * The app needs to pick up the trace ID from this header and put it into this field.
     * For example: Server-Timing: intid;desc=bd777df70e5e5356
     * In this case the field should hold the value bd777df70e5e5356.
     * This allows us to build a connection between end-user (mobile monitoring) and backend activity (tracing).
     */
    fun setBackendTraceId(@Size(max = 128) value: String) {
        stringMap["bt"] = value.truncate(128, "Backend Trace ID")
    }

    /**
     * The type of the beacon.
     */
    fun setType(value: BeaconType) {
        stringMap["t"] = value.internalType
    }

    internal fun getType():String{
        return stringMap["t"]?:""
    }

    /**
     * The kind of view/page/screen the user is on, e.g. login, checkout…
     */
    fun setView(@Size(max = 256) value: String) {
        stringMap["v"] = value.truncate(256, "View Name")
    }

    fun getView(): String? = stringMap["v"]

    /**
     * Defines what kind of event has happened on your website that should result in the transmission of a custom beacon.
     */
    fun setCustomEventName(@Size(max = 256) value: String) {
        stringMap["cen"] = value.truncate(256, "Custom Event Name")
    }

    internal fun getCustomEventName():String{
        return stringMap["cen"]?:""
    }

    /**
     * Any custom metric that can be passed with custom events Example: 123.345
     */
    fun setCustomMetricData(value: Double) {
        doubleMap["cm"] = value
    }

    /**
     * Custom key/value pairs that users can define within their App.
     *
     * To be transmitted to be backend as multiple m_ prefixed key value pairs. For example:
     *
     * m_user  Tom Mason
     * m_email tom.mason@example.com
     *
     * @Size(max=64)
     */
    fun setMeta(@Size(max = 98) key: String, @Size(max = 1024) value: String) {
        stringMap["m_${key.truncate(98, "Meta Key")}"] = value.truncate(1024, "Meta Value")
    }

    fun getMeta(key: String): String? = stringMap["m_$key"]

    /**
     * An identifier for the user. (optional)
     */
    fun setUserId(@Size(max = 128) value: String) {
        stringMap["ui"] = value.truncate(128, "User ID")
    }

    /**
     * The user name. (optional)
     */
    fun setUserName(@Size(max = 128) value: String) {
        stringMap["un"] = value.truncate(128, "User Name")
    }

    /**
     * The user’s email address. (optional)
     */
    fun setUserEmail(@Size(max = 128) value: String) {
        stringMap["ue"] = value.truncate(128, "User Email")
    }

    /**
     * The current selected language for the app
     * The language is described using BCP 47 language tags.
     *
     * For example: en-US
     */
    fun setUserLanguages(@Size(max = 5) value: List<Locale>) {
        stringMap["ul"] = value.take(5).joinToString(separator = ",") { it.bcp47() }
    }

    /**
     * The current eum mobile features in use
     *
     * For example: c for crash
     */
    fun setMobileFeatures(@Size(max = 50) value: List<MobileFeature>) {
        stringMap["uf"] = value.take(50).joinToString(separator = ",") { it.internalType }
    }

    /**
     * The bundle identifier uniquely identifies an app. Two apps cannot have the same bundle identifier.
     * To avoid conflicts, developers should use reverse domain name notation for choosing an app's bundle identifier
     * (e.g. com.instana.demoapp.android). The bundle identifier can contain a suffix for the staging environment
     * (e.g. .test or .dev).
     */
    fun setBundleIdentifier(@Size(max = 128) value: String) {
        stringMap["bi"] = value.truncate(128, "Bundle Identifier")
    }

    /**
     * AppBuild specifies the build version number of the bundle, which identifies an iteration (released or unreleased) of the bundle
     * The AppBuild is unique for each AppVersion and should be incremented with each deployed build.
     *
     * For example: 1203
     */
    fun setAppBuild(@Size(max = 128) value: String) {
        stringMap["ab"] = value.truncate(128, "App Build")
    }

    /**
     * AppVersion specifies the version for each store release. The AppVersion should conform to the semantic versioning.
     *
     * For example: 1.3.1
     */
    fun setAppVersion(@Size(max = 128) value: String) {
        stringMap["av"] = value.truncate(128, "App Version")
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
        //TODO I don't see how to reliable get this now (android/androidtv/googletv/...). For reference, in iOS it is "UIDevice.current.systemName"
        stringMap["osn"] = value.truncate(128, "OS Name")
    }

    /**
     * The OS version of the platform without any information about OS name itself.
     *
     * For example: 12.0.1
     */
    fun setOsVersion(@Size(max = 128) value: String) {
        //TODO there's "release" (10, 9, KIT_KAT, ...) and "sdk_int" (1, 2, 3, 4, 5...)
        stringMap["osv"] = value.truncate(128, "OS Version")
    }

    /**
     * For example: Apple
     */
    fun setDeviceManufacturer(@Size(max = 128) value: String) {
        stringMap["dma"] = value.truncate(128, "Device Manufacturer")
    }

    /**
     * For example: iPhone 6 XS
     */
    fun setDeviceModel(@Size(max = 128) value: String) {
        stringMap["dmo"] = value.truncate(128, "Device Model")
    }

    /**
     * For example: MP1.0
     */
    fun setDeviceHardware(@Size(max = 128) value: String) {
        //TODO is this ok? Right now, sending the result to "/proc"
        stringMap["dh"] = value.truncate(128, "Device Hardware")
    }

    /**
     * Whether the mobile device is rooted / jailbroken. True indicates that the device is definitely rooted / jailbroken.
     * False indicates that it isn't or that we could not identify the correct it.
     */
    fun setRooted(value: Boolean) {
        booleanMap["ro"] = value
    }

    fun getRooted(): Boolean? {
        return booleanMap["ro"]
    }

    /**
     * Whether the mobile device has the Google Play Services installed. Not having these installed can denote a
     * source of errors. True indicates that the platform is [Platform.ANDROID] and that the
     * Google Play Services are definitely missing. False indicates that it isn't applicable or that they are installed.
     */
    fun setGooglePlayServicesMissing(value: Boolean) {
        booleanMap["gpsm"] = value
    }

    fun getGooglePlayServicesMissing(): Boolean? {
        return booleanMap["gpsm"]
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
        stringMap["cn"] = value.truncate(256, "Carrier Name")
    }

    /**
     * The connection type
     *
     * https://wicg.github.io/netinfo/#connectiontype-enum
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
        stringMap["hu"] = value.truncate(4096, "HTTP call URL")
    }

    internal fun getHttpCallUrl():String{
        return stringMap["hu"]?:""
    }

    /**
     * The request's http method.
     *
     * For example: POST
     */
    fun setHttpCallMethod(@Size(max = 16) value: String) {
        stringMap["hm"] = value.truncate(16, "HTTP call METHOD")
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

    internal fun getHttpCallStatus():String{
        return (intMap["hs"]?:"").toString()
    }

    /**
     * HTTP headers in key/value pairs, in lower case.
     *
     * To be transmitted to backend as multiple h_ prefixed key value pairs. For example:
     *
     * h_host  example.com
     * h_content-type application/json
     *
     * Short serialization key prefix: h_
     *
     * @Size(max=64)
     */
    fun setHttpCallHeaders(@Size(max = 98) key: String, @Size(max = 1024) value: String) {
        stringMap["h_${key.truncate(98, "Header Key")}"] = value.truncate(1024, "Header Value")
    }

    /**
     * Used to get the header values in map format internally
     */
    internal fun getHttpCallHeaders(): Map<String, String> {
        return stringMap
            .filterKeys { it.startsWith("h_") }    // Filter keys that start with "h_"
            .mapKeys { (key, _) -> key.removePrefix("h_") }
    }

    /**
     * Only available for [BeaconType.HTTP_REQUEST]. Indicates the size of the encoded
     * (e.g. zipped) HTTP response body. Does not include the size of headers. Can be equal to [.setDecodedBodySize]
     * when the response is not compressed.
     *
     * -1 means that the value wasn't recorded.
     */
    fun setEncodedBodySize(@IntRange(from = -1) value: Long) {
        longMap["ebs"] = value
    }

    /**
     * Only available for [BeaconType.HTTP_REQUEST]. Indicates the size of the decoded
     * (e.g. unzipped) HTTP response body. Does not include the size of headers. Can be equal to [.setEncodedBodySize]
     * when the response is not compressed.
     *
     * -1 means that the value wasn't recorded.
     */
    fun setDecodedBodySize(@IntRange(from = -1) value: Long) {
        longMap["dbs"] = value
    }

    /**
     * Only available for [BeaconType.HTTP_REQUEST]. Indicates the total size of the HTTP
     * response including response headers and the encoded response body.
     *
     * -1 means that the value wasn't recorded.
     */
    fun setTransferSize(@IntRange(from = -1) value: Long) {
        longMap["trs"] = value
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
        value.truncate(16384, "Error Message").let {
            stringMap["em"] = it
            setErrorId(it.md5())
        }
    }

    internal fun getErrorMessage():String{
        return stringMap["em"]?:""
    }

    /**
     * error ID must be defined when there is an error message
     */
    private fun setErrorId(@Size(max = 128) value: String) {
        stringMap["ei"] = value.truncate(128, "Error ID")
    }

    /**
     * Type of the error
     * For iOS: You could use the ErrorDomain or the Swift Error enum case
     * For example: "NetworkError.timeout"
     */
    fun setErrorType(@Size(max = 1024) value: String) {
        stringMap["et"] = value.truncate(1024, "Error Type")
    }

    /**
     * A stacktrace of active threads.
     */
    fun setStackTrace(@Size(max = 16384) value: String) {
        stringMap["st"] = value.truncate(16384, "StackTrace")
    }

    /**
     * A stacktrace of active threads.
     */
    fun setAllStackTraces(@Size(max = 5242880) value: String) {
        stringMap["ast"] = value.truncate(5242880, "AllStackTraces")
    }

    /**
     * set Unique Id for users, this is to identify every users Uniquely when there is no userId
     * to be provided.
     *
     * For Example: If agent is used in a weather app there wont be any user data, but instana has
     * to identify the user for analytics purpose [crash affected etc.]
     *
     * Keeping the length limit to 128 to align website beacons
     */
    fun setUserSessionId(@Size(max = 128) value: String) {
        stringMap["usi"] = value.take(128)
    }

    /**
     * This decides to what type of performance beacon is being send
     */
    fun setPerformanceSubType(@Size(max = 10) value: String){
        stringMap["pst"] = value
    }

    /**
     * First-time launch (app is not in memory), Time in milli seconds
     */
    fun setAppColdStart(@IntRange(from = 1) value: Long) {
        longMap["acs"] = value
    }

    /**
     * The app was in memory but not actively running (after being idle), Time in milli seconds
     */
    fun setAppWarmStart(@IntRange(from = 1) value: Long) {
        longMap["aws"] = value
    }

    /**
     * The app was in the foreground or already running in the background, Time in milli seconds
     */
    fun setAppHotStart(@IntRange(from = 1) value: Long) {
        longMap["ahs"] = value
    }

    /**
     *  The total memory capacity available on the device., Value in Mb
     */
    fun setMaximumMb(@IntRange(from = 1) value: Long) {
        longMap["mmb"] = value
    }

    /**
     * The free memory currently available for apps and processes to use., Value in Mb
     */
    fun setAvailableMb(@IntRange(from = 1) value: Long) {
        longMap["amb"] = value
    }

    /**
     * The memory already allocated and actively being used by the system and apps, Value in Mb
     */
    fun setUsedMb(@IntRange(from = 1) value: Long) {
        longMap["umb"] = value
    }

    /**
     * Set view related meta data with this map, used in auto view capture mechanism
     */
    fun setInternalMeta(@Size(max = 64) key: String, @Size(max = 1024) value: String) {
        stringMap["im_${key.truncate(64, "View Meta Key")}"] = value.truncate(1024, "View Meta Value")
    }

    fun getViewMeta(key: String): String? = stringMap["im_$key"]

    internal fun getInternalMetaForView():Map<String,String>{
         return stringMap
            .filterKeys { it.startsWith("im_act") || it.startsWith("im_frag") }    // Filter keys that start with "h_"
            .mapKeys { (key, _) -> key.removePrefix("im_") }
    }

    @Suppress("DuplicatedCode") // I rather duplicate a few lines of code and keep type safety
    override fun toString(): String {
        val sb = StringBuilder()
        for (it in booleanMap) {
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        for (it in intMap) {
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        for (it in longMap) {
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        for (it in stringMap) {
            sb.append(it.key)
                .append("\t")
                .append(it.value.escape())
                .append("\n")
        }
        for (it in doubleMap){
            sb.append(it.key)
                .append("\t")
                .append(it.value)
                .append("\n")
        }
        return sb.toString()
    }

    private fun String.truncate(maxLength: Int, humanReadableDescription: String): String {
        if (this.length > maxLength) {
            Logger.e("$humanReadableDescription cannot be longer than $maxLength characters. Provided value will be truncated to allowed max length: '$this'")
        }
        return this.take(maxLength)
    }

    @VisibleForTesting
    private fun Locale.bcp47(): String = "$language-$country"

    @VisibleForTesting
    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    @VisibleForTesting
    private fun String.escape(): String =
        replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\t", "\\t")

    /**
     * When the native agent is utilised, the agent version will be only assigned else it will be in a
     * custom format `<native-agent-version>:<f>/<r>:<hybrid-agent-version>`
     */
    private fun retrieveVersionName():String{
        val androidAgentVersion = BuildConfig.AGENT_VERSION_NAME
        return when(Instana.config?.hybridAgentId){
            Platform.ANDROID.internalType,"",null -> androidAgentVersion
            else -> {
                "${androidAgentVersion}:${Instana.config?.hybridAgentId}:${Instana.config?.hybridAgentVersion}"
            }
        }
    }

    companion object {
        fun newSessionStart(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            connectionProfile: ConnectionProfile,
            userProfile: UserProfile,
            sessionId: String,
            view: String?,
            meta: Map<String, String>
        ): Beacon {
            return Beacon(BeaconType.SESSION_START, 0, appKey, sessionId, 0, appProfile, deviceProfile, connectionProfile, userProfile)
                .apply {
                    view?.run { setView(this) }
                    for (it in meta) { setMeta(it.key, it.value) }
                }
        }

        fun newHttpRequest(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            connectionProfile: ConnectionProfile,
            userProfile: UserProfile,
            sessionId: String,
            view: String?,
            meta: Map<String, String>,
            duration: Long,
            method: String?,
            url: String,
            headers: Map<String, String>,
            backendTraceId: String?,
            responseCode: Int?,
            requestSizeBytes: Long?, //TODO ignored?
            encodedResponseSizeBytes: Long?,
            decodedResponseSizeBytes: Long?,
            error: String?,
            requestStartTime: Long?
        ): Beacon {
            val errorCount = if (error != null || responseCode in 400..599) 1L else 0L
            return Beacon(BeaconType.HTTP_REQUEST, duration, appKey, sessionId, errorCount, appProfile, deviceProfile, connectionProfile, userProfile)
                .apply {
                    view?.run { setView(this) }
                    requestStartTime?.run { if (requestStartTime!=0L) { setTimestamp(requestStartTime) } }
                    for (it in meta) { setMeta(it.key, it.value) }
                    method?.run { setHttpCallMethod(this) }
                    setHttpCallUrl(url)
                    for (it in headers) { setHttpCallHeaders(it.key, it.value) }
                    responseCode?.run { setHttpCallStatus(this) }
                    encodedResponseSizeBytes?.run { setEncodedBodySize(this) }
                    decodedResponseSizeBytes?.run { setDecodedBodySize(this) }
                    backendTraceId?.run { setBackendTraceId(backendTraceId) }
                    error?.run { setErrorMessage(this) }
                }
        }

        fun newViewChange(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            connectionProfile: ConnectionProfile,
            userProfile: UserProfile,
            sessionId: String,
            view: String,
            meta: Map<String, String>,
            viewMeta:Map<String,String>
        ): Beacon {
            return Beacon(BeaconType.VIEW_CHANGE, 0, appKey, sessionId, 0, appProfile, deviceProfile, connectionProfile, userProfile)
                .apply {
                    setView(view)
                    for (it in viewMeta) { setInternalMeta(it.key, it.value) }
                    for (it in meta) { setMeta(it.key, it.value) }
                }
        }

        fun newCustomEvent(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            connectionProfile: ConnectionProfile,
            userProfile: UserProfile,
            sessionId: String,
            view: String?,
            meta: Map<String, String>,
            startTime: Long,
            duration: Long,
            backendTraceId: String?,
            error: String?,
            name: String,
            customMetric: Double?
        ): Beacon {
            val errorCount = if (error != null) 1L else 0L
            return Beacon(BeaconType.CUSTOM, duration, appKey, sessionId, errorCount, appProfile, deviceProfile, connectionProfile, userProfile)
                .apply {
                    view?.run { setView(this) }
                    for (it in meta) { setMeta(it.key, it.value) }
                    setCustomEventName(name)
                    setTimestamp(startTime)
                    backendTraceId?.run { setBackendTraceId(backendTraceId) }
                    error?.run { setErrorMessage(this) }
                    customMetric?.run { setCustomMetricData(this) }
                }
        }

        fun newDropBeacon(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            connectionProfile: ConnectionProfile,
            userProfile: UserProfile,
            sessionId: String,
            view: String?,
            internalMeta: Map<String, String>,
            startTime: Long,
        ): Beacon {
            return Beacon(BeaconType.DROP_BEACON, 0, appKey, sessionId, 0, appProfile, deviceProfile, connectionProfile, userProfile)
                .apply {
                    view?.run { setView(this) }
                    setTimestamp(startTime)
                    for (it in internalMeta) {
                        setInternalMeta(key = it.key,value = it.value)
                    }
                }
        }

        internal fun newPerformanceBeacon(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            connectionProfile: ConnectionProfile,
            userProfile: UserProfile,
            sessionId: String,
            view: String?,
            performanceMetric:PerformanceMetric
        ):Beacon{
            val baseBeacon = Beacon(BeaconType.PERFORMANCE,0,appKey,sessionId,0,appProfile,deviceProfile,connectionProfile,userProfile).apply {
                view?.run { setView(this) }
            }
            return when(performanceMetric){
                is PerformanceMetric.AppStartTime ->{
                     baseBeacon.apply {
                        setPerformanceSubType(PerformanceSubType.APP_START_TIME.internalType)
                        performanceMetric.run {
                            coldStart.takeIf { it != 0L }?.let { setAppColdStart(it) }
                            warmStart.takeIf { it != 0L }?.let { setAppWarmStart(it) }
                            hotStart.takeIf { it != 0L }?.let { setAppHotStart(it) }
                        }
                    }
                }
                is PerformanceMetric.AppNotResponding ->{
                     baseBeacon.apply {
                        setPerformanceSubType(PerformanceSubType.ANR.internalType)
                        performanceMetric.duration?.let {
                            setDuration(it)
                        }
                        setStackTrace(performanceMetric.stackTrace)
                        setAllStackTraces(performanceMetric.allStackTrace)
                    }
                }
                is PerformanceMetric.OutOfMemory ->{
                     baseBeacon.apply {
                        setPerformanceSubType(PerformanceSubType.OUT_OF_MEMORY.internalType)
                        setAvailableMb(performanceMetric.availableMb)
                        setMaximumMb(performanceMetric.maximumMb)
                        setUsedMb(performanceMetric.usedMb)
                    }
                }
            }
        }

        fun newCrash(
            appKey: String,
            appProfile: AppProfile,
            deviceProfile: DeviceProfile,
            connectionProfile: ConnectionProfile,
            userProfile: UserProfile,
            sessionId: String,
            view: String?,
            meta: Map<String, String>,
            error: String?,
            errorType: String?,
            stackTrace: String?,
            allStackTraces: String?
        ): Beacon {
            // TODO might need to set batchSize
            return Beacon(BeaconType.CRASH, 0, appKey, sessionId, 0, appProfile, deviceProfile, connectionProfile, userProfile)
                .apply {
                    view?.run { setView(this) }
                    for (it in meta) { setMeta(it.key, it.value) }
                    error?.run { setErrorMessage(this) }
                    errorType?.run { setErrorType(this) }
                    stackTrace?.run { setStackTrace(this) }
                    allStackTraces?.run { setAllStackTraces(this) }
                }
        }

        // Hack way to add meta key/value pair to beacon string before sending to server
        fun addMetaData(beaconStr: String, key: String, value: String): String {
            val meta = "m_$key\t$value\n"
            return beaconStr + meta
        }
    }
}
