# Android Instana Agent 

## Getting started

Please head over to the [official Instana Mobile App Monitoring documentation](https://docs.instana.io/products/mobile_app_monitoring/) to get all the details about the usage of Instana Android Agent.

For a quick start with a minimum configuration, the following steps shall suffice:

### Before beginning

Make sure that the Google, Maven Central and JCenter's Maven repositories are included in your project-level `build.gradle` file:

```groovy
buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
```

### 1. Add Instana Agent SDK to your app
In your module (app-level) Gradle file (usually `app/build.gradle`):
```groovy
dependencies {
    implementation 'com.instana:android-agent-runtime:1.0.0-SNAPSHOT'
}
```

### 2. Add Instana Agent Plugin to your app
In your module (app-level) Gradle file (usually `app/build.gradle`), after applying the `com.android.application` plugin:
```groovy
apply plugin: 'com.android.application'
apply plugin: 'com.instana.android-agent-plugin'
```

In your module (app-level) Gradle file (usually `app/build.gradle`):
```groovy
buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath "com.instana:android-agent-plugin:1.0.0-SNAPSHOT"
    }
}
```

### 3. Initialize Instana Agent when your app starts

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

### 4. Add Java 1.8 compatibility

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

### Supported network clients

Instana Android Agent is currently capable of automatically tracking events for the following network clients:
- [OkHttp3](https://github.com/square/okhttp/)
- [HttpURLConnection](https://developer.android.com/reference/java/net/HttpURLConnection)

You can use *manual tracking* to add support for any client yourself, or please consider [contributing](#contributing) to the project.

### Additional configuration settings

The configuration described in `Step 3` is the minimum configuration you must provide to Instana Agent to function. 

Please check for additional options in the [Android API documentation](https://documentation.link).

## Examples

Please head over to the [android-agent-examples repository](https://github.com/instana/android-agent-examples) to find multiple usage examples of the Instana Android Agent.

You can also find an example in this repo's `instana-example` folder. Just please be aware that this is an example meant to be used during the development of the Android Agent, and therefore might contain usages of the Agent that are more complex that what you need for your situation.

## Contributing 

### Components

- `instana-example`: demo app covering (most of) the usage scenarios of the Agent 
- `plugin`: gradle plugin to simplify configuration for apps
- `runtime`: weaving logic, instrumentation, beacon handling, ...

### Building `instana-example`

Please head over to it's specific [README.md](instana-example/README.md) to learn how to build `instana-example`.

### Building Instana Android Agent

You must use IntelliJ Ultimate IDE in order to compile the `plugin` and `runtime`.

If you want to work on the `runtime` or `plugin` components of the Agent, you must publish them to your local maven repository so they can be found by your application.

Relevant commands:
- compile and publish `runtime` to local maven: 
```shell script
gradlew :runtime::publishToMavenLocal
```
- compile and publish `plugin` to local maven:
```shell script
gradlew :plugin::publishToMavenLocal
```
