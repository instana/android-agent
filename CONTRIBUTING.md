# Contributing 

## How to Contribute

This is an open source project, and we appreciate your help!

In order to clarify the intellectual property license granted with contributions from any person or entity, a Contributor License Agreement ("CLA") must be on file that has been signed by each contributor, indicating agreement to the license terms below. This license is for your protection as a contributor as well as the protection of Instana and its customers; it does not change your rights to use your own contributions for any other purpose.

Please print, fill out, and sign the [contributor license agreement](https://github.com/instana/nodejs-sensor/raw/main/misc/instana-nodejs-cla-individual.pdf). Once completed, please scan the document as a PDF file and email to the following email address: ben@instana.com.

Thank you for your interest in the Instana Android project!

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
