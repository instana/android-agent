Changelog
==========

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
