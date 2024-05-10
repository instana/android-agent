# Android Instana Agent <a href="https://mvnrepository.com/artifact/com.instana/android-agent-plugin"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.instana/android-agent-plugin?color=0db4b3"></a>

**[Changelog](CHANGELOG.md)** |
**[Contributing](CONTRIBUTING.md)** |
**[Support](https://ibm.biz/Instana-support)**

---

## Requirements

- Android 4.1+ (API level 16+) 
- Java 8+ 
- Gradle 7.3.3+
- AndroidX
- targetSdk 33+

Note:
  If you have to use Gradle version lower than 7.3.3 in your project, please use android-agent version lower than 6.0.0.

## Getting started

Please head over to the [official Instana Mobile App Monitoring documentation](https://ibm.biz/android-monitoring-api) to get all the details about the usage of Instana Android Agent.

For a quick start with a minimum configuration, the following steps shall suffice:

### Before beginning

Make sure that the Google and MavenCentral repositories are included in your project-level `build.gradle` file:

```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

### Step 1. Add Instana Agent SDK to your app
In your module (app-level) Gradle file (usually `app/build.gradle`):
```groovy
dependencies {
    implementation 'com.instana:android-agent-runtime:6.0.18'
}
```
While using kotlin-scripts, In (app-level) Gradle file (usually `app/build.gradle.kts`):
```groovy
dependencies {
  implementation("com.instana:android-agent-runtime:6.0.18")
}
```


### Step 2. Add Instana Agent Plugin to your app
In your module (app-level) Gradle file (usually `app/build.gradle`), after applying the `com.android.application` plugin:
```groovy
apply plugin: 'com.android.application'
apply plugin: 'com.instana.android-agent-plugin'
```
While using kotlin-scripts, In (app-level) Gradle file (usually `app/build.gradle.kts`):
```groovy
plugins {
  id("com.android.application")
  id("com.instana.android-agent-plugin")
}
```

In your module (app-level) Gradle file (usually `app/build.gradle`):
```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath "com.instana:android-agent-plugin:6.0.18"
    }
}
```
While using kotlin-scripts, In (project-level) Gradle file (usually `/build.gradle.kts`):
```groovy
buildscript {
  dependencies {
    classpath("com.instana:android-agent-plugin:6.0.18")
  }
}
```

### Step 3. Initialize Instana Agent when your app starts

In your class extending `Application`, replace `YOUR_REPORTING_URL` and `YOUR_APP_KEY` with the configuration values you'll find in your Instana Dashboard:
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Instana.setup(
            this,
            InstanaConfig(
                reportingURL = "YOUR_REPORTING_URL",
                key = "YOUR_APP_KEY"
            )
        )
    }
}
```

### Step 4. Add Java 1.8 compatibility

Note: this step is not required if your `minSdkVersion` is 24 or higher

In your module (app-level) Gradle file (usually `app/build.gradle`):
```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

### Step 5 (optional). Request READ_PHONE_STATE permission

When a user accesses the Internet through a cellular network, Instana Agent has the ability to report the specific type of cellular network. 

In order to enable the reporting of the cellular network type, your app needs to request the `READ_PHONE_STATE` permission. Please refer to the [`Request App Permissions` section in the official Android documentation](https://developer.android.com/training/permissions/requesting). 

If your app doesn't request the permission or if the user declines it, Instana Agent will simply not report the cellular network type. 

### Supported network clients

Instana Android Agent is currently capable of automatically tracking events for the following network clients:
- [OkHttp3](https://square.github.io/okhttp/)
- [HttpURLConnection](https://developer.android.com/reference/java/net/HttpURLConnection)
- [Retrofit](https://square.github.io/retrofit/)

You can use *manual tracking* to add support for any client yourself, or please consider [contributing](CONTRIBUTING.md) to the project.

### Additional configuration settings

The configuration described in `Step 3` is the minimum configuration you must provide to Instana Agent to function. 

Please check for additional options in the [Android API documentation](https://www.ibm.com/docs/en/instana-observability/current?topic=monitoring-android-api).

## Examples

Please head over to the [android-agent-examples repository](https://github.com/instana/android-agent-examples) to find multiple usage examples of the Instana Android Agent.

You can also find an example in this repo's `instana-example` folder. Just please be aware that this is an example meant to be used during the development of the Android Agent, and therefore might contain usages of the Agent that are more complex that what you need for your situation.
