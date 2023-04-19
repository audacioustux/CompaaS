#!/usr/bin/env bash

set -e

source "${SDKMAN_DIR}/bin/sdkman-init.sh"

install_graalvm() {
    if [ -n "${GRAAL_EE_DOWNLOAD_TOKEN}" ]; then
        GRAALVM_EDITION="ee"
    else
        GRAALVM_EDITION="ce"
    fi

    GRAALVM_VERSION=${GRAALVM_VERSION:-"22.3.1"}
    JAVA_VERSION=${JAVA_VERSION:-"java17"}
    GRAALVM_COMPONENTS=${GRAALVM_COMPONENTS:-"native-image,nodejs,wasm"}

    GRAALVM_NAME=graalvm-${GRAALVM_EDITION}-${JAVA_VERSION}-${GRAALVM_VERSION}

    curl -sL https://get.graalvm.org/jdk | bash -s -- $GRAALVM_NAME \
    --to $HOME/ \
    -c $GRAALVM_COMPONENTS

    GRAALVM_HOME="$HOME/${GRAALVM_NAME}"

    SDK_GRAALVM_ID=${GRAALVM_VERSION:0:6}.r${JAVA_VERSION:4:2}-grl${GRAALVM_EDITION:0:1}
    sdk install java ${SDK_GRAALVM_ID} ${GRAALVM_HOME}
    sdk default java ${SDK_GRAALVM_ID}
}

install_sbt() {
    sdk install sbt
}

install_graalvm
install_sbt
