# Contributing 

## How to Contribute

This is an open source project, and we appreciate your help!

Each source file must include this license header:

```
/*
 * (c) Copyright IBM Corp. 2024
 */
```

Furthermore you must include a sign-off statement in the commit message.

> Signed-off-by: John Doe <john.doe@example.com>

### Please note that in the case of the below-mentioned scenarios, follow the specified steps:
- **Proposing New Features**: Vist the ideas portal for [Cloud Management and AIOps](https://automation-management.ideas.ibm.com/?project=INSTANA) and post your idea to get feedback from IBM. This is to avoid you wasting your valuable time working on a feature that the project developers are not interested in accepting into the code base.
- **Raising a Bug**: Please visit [IBM Support](https://www.ibm.com/mysupport/s/?language=en_US) and open a case to get help from our experts.
- **Merge Approval**: The codeowners use LGTM (Looks Good To Me) in comments on the code review to indicate acceptance. A change requires LGTMs from two of the members. Request review from @instana/eng-eum for approvals.

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
