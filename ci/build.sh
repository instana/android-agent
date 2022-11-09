#!/bin/bash
set -e
set -o pipefail
set -x


echo skip to debug push_github_com job 3
exit 0

apk add --no-progress gettext
which envsubst

# env for Android SDK
export ANDROID_SDK_ROOT="/opt/sdk"
export ANDROID_HOME=${ANDROID_SDK_ROOT}
export CMDLINE_VERSION="7.0"
export SDK_TOOLS="8512546"
export BUILD_TOOLS="32.0.0"
export TARGET_SDK=32
export JDK_VERSION=8
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
echo install Android SDKs
sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --install "platform-tools" "extras;google;instantapps" "build-tools;${BUILD_TOOLS}" "platforms;android-${TARGET_SDK}"

# check versions
mvn -version

ls -lh /opt/sdk/

./gradlew build

ls -lh build
ls -lh plugin/build
ls -lh runtime/build

echo "${PGP_KEY_PASSWORD}" > pass.txt
echo "${PGP_KEY}" > sign.key

export PGP_KEY_FILE=$(pwd)/sign.key
export PGP_KEY_PASSWORD_FILE=$(pwd)/pass.txt
envsubst < publish.properties.example > publish.properties

ls -lh

echo "[NOTE] disable new publishment until we got the pipeline PR fully merged!"
# echo Publish Instana Android Plugin and Agent
# ./gradlew --stacktrace publish closeAndReleaseRepository

echo Done.
