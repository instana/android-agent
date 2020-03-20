# Android Instana Agent example

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

### Supported network clients

Instana Android Agent is currently capable of automatically tracking events for the following network clients:
- [OkHttp3](https://github.com/square/okhttp/)
- [HttpURLConnection](https://developer.android.com/reference/java/net/HttpURLConnection)

You can use *manual tracking* to add support for any client yourself, or please consider [contributing](#contributing) to the project.

### Additional configuration settings

The configuration described in `Step 3` is the minimum configuration you must provide to Instana Agent to function. 

Please check for additional options in the [Android API documentation](https://documentation.link).

## Contributing 

### Components

- `instana-example`: demo app covering (most of) the usage scenarios of the Agent 
- `plugin`: gradle plugin to simplify configuration for apps
- `runtime`: weaving logic, instrumentation, beacon handling, ...

### Configure `app`

Copy [app/instana.properties.example](app/instana.properties.example) to `app/instana.properties` and replace the placeholders in `app/instana.properties` with your Instana *app key* and your *reporting url*. If you don't have any Instana credentials, please [create a Trial account](https://www.instana.com/trial/)

Review and tweak Instana configuration in [app/src/main/java/com/instana/mobileeum/DemoApp.kt](app/src/main/java/com/instana/mobileeum/DemoApp.kt).

### Build types

The `app` module currently has 2 build types:
- `debug`: debuggable, allows the use of user certificates installed in your device to proxy all request/responses (both encrypted and unencrypted)
- `release`: obfuscated and shrunk using R8

### Compile

You must use the full IntelliJ Ultimate IDE in order to compile the `plugin` and `runtime`.

If you want to work on the `runtime` or `plugin` components of the Agent, you must publish them to your local maven repository so they can be found by the `app`.

Relevant commands:
- compile and publish `runtime` to local maven: 
```shell script
gradlew :runtime::publishToMavenLocal
```
- compile and publish `plugin` to local maven:
```shell script
gradlew :plugin::publishToMavenLocal
```
- assemble `app`:
```shell script
:app::assemble
```
