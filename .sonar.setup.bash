#!/usr/bin/env bash

REPO_DIR=`pwd`

yum update
yum install -y maven java-11-openjdk wget

export JDK_VERSION=11
export JAVA_HOME="/usr/lib/jvm/jre-11-openjdk"
export SONAR_EXEC_GRADLE=true

# install android SDK
export ANDROID_SDK_ROOT="/opt/sdk"
export ANDROID_HOME=${ANDROID_SDK_ROOT}
export CMDLINE_VERSION="9.0"
export SDK_TOOLS="9477386"
export BUILD_TOOLS="33.0.0"
export TARGET_SDK=33
export PATH=$PATH:${ANDROID_SDK_ROOT}/cmdline-tools/${CMDLINE_VERSION}/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/extras/google/instantapps

# install Android Command line tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-${SDK_TOOLS}_latest.zip -O /tmp/tools.zip
mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    unzip -qq /tmp/tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/* ${ANDROID_SDK_ROOT}/cmdline-tools/${CMDLINE_VERSION} && \
    rm -v /tmp/tools.zip && \
    mkdir -p ~/.android/ && touch ~/.android/repositories.cfg

echo Accept Android SDK licenses
which yes
yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses &> /dev/null || echo "licenses accepted"

# install Android SDK
echo Install Android SDKs
sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --install "platform-tools" "extras;google;instantapps" "build-tools;${BUILD_TOOLS}" "platforms;android-${TARGET_SDK}"

# check versions
java -version
mvn -version
ls -lh /opt/sdk/

# generate unittest coverage report. Currently only add unittest for runtime module 
echo Generate unittest coverage report
cd $REPO_DIR
if [ -f "runtime/build/reports/coverage/test/debug/report.xml" ]; then
    rm -rf runtime/build/reports/coverage/test/debug/*
fi

if [ -f "plugin/build/reports/jacoco/jacocoTestReportPlugin/jacocoTestReportPlugin.xml" ]; then
    rm -rf plugin/build/reports/jacoco/jacocoTestReportPlugin/*
fi

./gradlew runtime:createDebugUnitTestCoverageReport
./gradlew plugin:jacocoTestReportPlugin

# Put the coverage report in runtime module folder
mkdir -p runtime/build/reports/jacoco/test/
mkdir -p plugin/build/reports/jacoco/test/

cp runtime/build/reports/coverage/test/debug/report.xml runtime/build/reports/jacoco/test/jacocoTestReport.xml
cp plugin/build/reports/jacoco/jacocoTestReportPlugin/jacocoTestReportPlugin.xml plugin/build/reports/jacoco/test/jacocoTestReport.xml

yum uninstall -y java-11-openjdk
export JDK_VERSION=21
export JAVA_HOME="/usr/lib/jvm/jre-21-openjdk"