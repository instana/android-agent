Changelog
==========
## Version 6.0.18

_2024-05-08_

- `RateLimiter` Updated, new limits are 20 beacons/10s & 500 beacons/5min.

_2024-05-15_

- Introduced extension function `addInstanaObserver` that could capture composable screen names from `NavHostController`

_2024-05-22_

- `validateAllKeys` extension function introduced to filter valid keys from internal meta, usage: internal to agent
- sonar gradle version updated

## Version 6.0.17

_2024-04-23_

- Enhanced runBlocking for crash reporting by instantly writing data to disk, bypassing initial delay.

## Version 6.0.16

_2024-04-18_

- `viewMeta` Exposed for bridging with cross-platform agents

## Version 6.0.15

_2024-02-28_

- `autoCaptureScreenNames` was introduced to auto capture the Fragment & Activity names

_2024-03-07_

- added more coverage with unit test

## Version 6.0.14

_2024-02-13_

- update vulnerable transitive dependency from gradle

_2024-02-14_

- added `customMetric` feature to custom events

- added more unit tests

## Version 6.0.13

_2024-02-07_

- Fix for retrieving headers based on `java.net.URLConnection.getHeaderField`
- Minor gradle file path updates in example app

## Version 6.0.12

_2024-01-18_

- `AgentVersion` updated to a new format for differentiating hybrid-agent and native agent data 

## Version 6.0.11

_2023-12-18_

- Stale beacons clearing logic added

## Version 6.0.9

_2023-12-05_

- `Instana.captureHeaders`, `Instana.ignoreURLs`, `Instana.redactHTTPQuery` type updated to `Collections.synchronizedList`, to avoid concurrent update exceptions

## Version 6.0.8

_2023-09-19_

- Version update strategy changed with SSOT from `version.gradle` file.

_2023-10-17_

- `README.md` file updated with kotlin-script support details

_2023-11-07_

- Change beacon id from 128 bit UUID to 64 bit hex string

_2023-11-10_

- ASM 9 upgrade to support JAVA_17 with sealed classes

## Version 6.0.7

_2023-09-14_

- Modifying property declarations for variables within the constructor of Instana Configuration to accommodate updates in Java-based configuration.

## Version 6.0.6

_2023-08-17_

- `usiRefreshTimeIntervalInHrs` is included to manage the frequency of refreshing the `usi` used for custom identification.

_2023-07-26_

- Add crash collection enablement status to mobile feature list and send to Instana backend

## Version 6.0.5

_2023-07-24_

- Fix duplicated beacons issue
- Once in slow sending mode, periodically send 1 beacon. Once out of slow sending mode, flush all beacons immediately.

## Version 6.0.4

_2023-07-08_

- Apply privacy redaction policy to all HTTP beacons 

## Version 6.0.3

_2023-06-22_

- Allow http capture for Google Ads

## Version 5.2.7

_2023-06-22_

- Allow http capture for Google Ads

## Version 6.0.2

_2023-06-10_

- Fix beacon loss issue when send beacons in one batch reached limit 100
- Fix instana-example app build issue if upgraded to gradle 8

_2023-06-13_

- Improve error handling on beacon send failure

## Version 6.0.1

_2023-05-10_

- Fix unit test for Android Studio Electric Eel built on February 17, 2023 or later
- Gradle 8 compatibility fix for instana-example
- Set view name for crash beacon

## Version 5.2.6

_2023-05-10_

- Upgrade android-agent runtime compileSdk version to 31
- Set view name for crash beacon

## Version 6.0.0

_2023-04-06_

- Upgrade AGP version to 7.2.2, Gradle version to 7.3.3, Kotlin version to 1.6.10
- Remove deprecated Transform API used by Instana plugin, improve app build time
- Upgrade android-agent targetSdk to 33, fix unit test cases
- Upgrade dependent library versions for android-agent and instana-example app
- Upgrade instana-example app targetSdk to 33, remove deprecated APIs

_2023-02-10_

- Upgrade android-agent runtime compileSdk version to 31

## Version 5.2.5-beta

_2022-11-21_

- fix `debugTrustInsecureReportingURL` to support all Android version

## Version 5.2.4

_2022-11-03_

- Fix exception handling for closed stream in HttpURLConnection
- Test purpose flag `debugTrustInsecureReportingURL` to allow self signed instana reporting url
- Ignore `EmbeddingAdapter` during auto instrumentation
- Remove usage of `Map.forEach` which not supported in lower platform
- Drop invalid old beacons with keys not in use
- Flag `initialSetupTimeoutMs` to wait for instana initialization if not in MainLooper
- Improve instana-example to demonstrate how to enable instana based on current build type

## Version 5.2.3

_2022-08-03_

- Fix issue with custom header capture and query parameters with no associated value

## Version 5.2.2

_2022-07-05_

- Fix custom header capture for manual tracking

## Version 5.2.1

_2022-06-06_

- Fix custom header capture for manual tracking

## Version 5.2.0

_2022-06-06_

- Add `Instana.captureHeaders` to allow capturing custom headers for each request/response

## Version 5.1.0

_2022-05-10_

- Add `Instana.redactHTTPQuery` to hide URL Query parameters

## Version 5.0.5

_2022-05-03_

- Fix `java.lang.VerifyError [...] but expected Reference: java.net.HttpURLConnection [...]` error
- Silence false error-reports in build-time (now configurable)

## Version 5.0.4

_2022-03-01_

- Report responses with server-error or client-error HTTP responses codes as failed
- Don't instrument NewRelic

## Version 5.0.3

_2021-12-10_

- Update log levels during compilation time
- Don't instrument Firebase

## Version 5.0.2

_2021-12-01_

- Update WorkManager to 2.7.1 to avoid crash in Android 12 ([ref in WorkManager Release Notes](https://developer.android.com/jetpack/androidx/releases/work#2.7.0-alpha02))
- Fix compilation errors in case-insensitive file-systems

## Version 5.0.1

_2021-10-20_

- Fix HttpURLConnection exception handling which caused `CIRCULAR REFERENCE` issues on R8 
- Skip internal GMS classes

## Version 5.0.0

_2021-10-11_

- Completely rewritten Plugin
- Removed strong dependency on Gradle version. Currently confirmed to work with Gradle versions up to 7.0.2
- Added support for incremental builds 
- New Groovy DSL for Plugin configuration
- Fixed `error indicator` for CustomEvents

## Version 4.7.0, 3.7.0, 2.7.0 and 1.7.0

_2021-09-15_

- New 'collectionEnabled' flag to enable/disable beacon collection and transmission on runtime
- Stop consuming exceptions in OkHttp Interceptor

## Version 4.6.2, 3.6.2, 2.6.2 and 1.6.2

_2021-07-23_

- Prevent possible crash when Instana is initialized late on the app lifecycle
- Prevent crash when filesystem is not writeable

## Version 4.6.1, 3.6.1, 2.6.1 and 1.6.1

_2021-07-12_

- Prevent issue with Instabug initialization

## Version 4.6.0, 3.6.0, 2.6.0 and 1.6.0

_2021-06-23_

- Increase the scope of automatically traced dependencies
- Improve logging

## Version 4.5.6, 3.5.6, 2.5.6 and 1.5.6

_2021-06-07_

- Fix trace timing stop-watch

## Version 4.5.5, 3.5.5, 2.5.5 and 1.5.5

_2021-04-28_

- Update connection-type identifier for wired/ethernet

## Version 4.5.4.1

_2021-03-30_

- Internal release. No code changes. Intended as a test to verify the new mavenCentral integration in the CI pipelines

## Version 4.5.4, 3.5.4, 2.5.4 and 1.5.4

_2021-03-22_

- Prevent crash on some Android 11 devices when trying to ACCESS_NETWORK_STATE
- Migrate to MavenCentral following the [deprecation of Jcenter](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)

## Version 4.5.3, 3.5.3, 2.5.3 and 1.5.3

_2021-03-02_

- Start `overweaving` to avoid issues with other tools that manipulate bytecode

## Version 4.5.2, 3.5.2, 2.5.2 and 1.5.2

_2021-02-10_

- Prevent possible issue when running instrumented tests or unit tests
- Explicitly exclude `firebase-perf` from transformation targets

## Version 4.5.1, 3.5.1, 2.5.1 and 1.5.1

_2020-12-07_

- Fix thread-safety issue on OkHttp3 tracing

## Version 4.5.0

_2020-11-25_

- New branch 4.x.x to support Android Gradle Plugin 4.1.x and Gradle 6.5+ 
- Feature-parity with v3.5.0

## Version 3.5.0, 2.5.0 and 1.5.0

_2020-11-02_

- Fix possible crash on Timer mishandling
- Improve API for Manual HTTP Tracing

## Version 3.4.1, 2.4.1 and 1.4.1

_2020-10-01_

- Fix possible crash when OkHttp3 Request instances are prematurely disposed
- Fix issue when redundant port numbers are provided in `InstanaConfig.reportingURL`
- Improved handling of the size limit for each reported field
- Addition of basic rate-limiting mechanism
- Reduce stress produced by the Agent on WorkManager (fixes possible Sqlite errors)

## Version 3.4.0, 2.4.0 and 1.4.0

_2020-08-10_

- Add support for API 16 (Android 4.1)
- Upgrade WorkManager to 2.4.0

## Version 3.3.2, 2.3.2 and 1.3.2

_2020-08-04_

- Fix minSdkVersion and set it to 21 (Android 5.0)

## Version 3.3.1,  2.3.1 and 1.3.1

_2020-08-03_

- Added a workaround to avoid crashes when WorkManager is not properly initialized
- Add support for Android 11 (API 30)

## Version 3.3.0, 2.3.0 and 1.3.0

_2020-06-29_

- New branch 3.x.x to support Android Gradle Plugin 4.0.0 and Gradle 6.1.1+
- Added Logger facade
- Fixed issue that could prevent project compilation when jar libraries where included in the project tree 

## Version 2.2.0 and 1.2.0

_2020-05-27_

- Added support for reporting of Custom Events
- Improved handling of server traces
- Fixed issue which could cause a crash when Instana Agent was used without being initialized

## Version 2.1.4 and 1.1.4

_2020-05-06_

- Fixed concurrency issue which could cause a crash for users running multithreaded HttpURLConnection clients

## Version 2.1.3 and 1.1.3

_2020-04-23_

- Fixed bug which could cause a crash for users running HttpURLConnection
- Report `osName` for Android 6.0 (23) devices (if defined by the manufacturer)

## Version 2.1.2 and 1.1.2

_2020-04-16_

- Fixed bug which could cause network requests in project-modules to not be traced 

## Version 2.1.1

_2020-04-14_

- New branch 2.x.x to support Android Gradle Plugin 3.6.x and Gradle 6.x
- Feature-parity with v1.1.1

## Version 1.1.1

_2020-04-03_

- Fixed bug which could potentially leave a file open after the execution of a WorkManager Work 
- Changes to make sure all file-access is done in the IO thread (this solves remaining StrictMode warnings)
- Fixed agent-version reported to Instana

## Version 1.1.0

_2020-04-01_

- Improved handling of uninitiated agent
- Added support for retrofit
- Added support for react-native 

## Version 1.0.0

_2020-03-25_

- Initial release
