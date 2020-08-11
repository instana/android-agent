# Contributing 

## Components

- `instana-example`: demo app covering (most of) the usage scenarios of the Agent 
- `plugin`: gradle plugin to simplify configuration for apps
- `runtime`: weaving logic, instrumentation, beacon handling, ...

## Building `instana-example`

Please head over to its specific [README.md](instana-example/README.md) to learn how to build `instana-example`.

## Building Instana Android Agent

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
