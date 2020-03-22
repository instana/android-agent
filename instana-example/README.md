# Android Instana Agent example

## Building

### Configure `app`

Copy [app/instana.properties.example](app/instana.properties.example) to `app/instana.properties` and replace the placeholders in `app/instana.properties` with your Instana *app key* and your *reporting url*. If you don't have any Instana credentials, please [create a Trial account](https://www.instana.com/trial/)

Review and tweak Instana configuration in [app/src/main/java/com/instana/mobileeum/DemoApp.kt](app/src/main/java/com/instana/mobileeum/DemoApp.kt).

### Build types

The `app` module currently has 2 build types:
- `debug`: debuggable, allows the use of user certificates installed in your device to proxy all request/responses (both encrypted and unencrypted)
- `release`: obfuscated and shrunk using R8

### Compile

Just assemble and install your preferred variant using Android Studio 3+ or the command line
