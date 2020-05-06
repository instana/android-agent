Changelog
==========

## Version 1.1.3

_2020-05-06_

- Fixed concurrency issue which could cause a crash for users running multithreaded HttpURLConnection clients

## Version 1.1.3

_2020-04-23_

- Fixed bug which could cause a crash for users running HttpURLConnection
- Report `osName` for Android 6.0 (23) devices (if defined by the manufacturer)

## Version 1.1.2

_2020-04-16_

- Fixed bug which could cause network requests in project-modules to not be traced 

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
