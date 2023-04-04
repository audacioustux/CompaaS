#!/usr/bin/env bash

set -e

install_sdkman() {
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
}

install_graalvm() {
    GRAALVM_EDITION=${GRAALVM_EDITION:-ce}
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

    export PATH=${GRAALVM_HOME}/bin:$PATH
    export JAVA_HOME=${GRAALVM_HOME}
}

install_sbt() {
    sdk install sbt
}

install_sdkman
install_graalvm
install_sbt
