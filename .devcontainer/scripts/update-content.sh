#!/usr/bin/env bash

set -eax

update-apt-pkgs() {
    sudo apt-get update
    sudo apt-get upgrade -y
}

update-npm-pkgs() {
    npm update -g
}

install-k9s() {
    curl https://webi.sh/k9s | sh
}

install-tilt() {
    curl https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
}

install-sdks() {
    source ~/.sdkman/bin/sdkman-init.sh

    if [ -n "${GRAAL_EE_DOWNLOAD_TOKEN}" ]; then
        GRAAL_EDITION="ee"
    else
        GRAAL_EDITION="ce"
    fi
    local GRAAL_VERSION=${GRAAL_VERSION:-"22.3.1"}
    local JAVA_VERSION=${JAVA_VERSION:-"17"}
    local GRAAL_COMPONENTS=${GRAAL_COMPONENTS:-"native-image,js,wasm,llvm-toolchain,espresso,python"}

    local GRAAL_RELEASE="graalvm-${GRAAL_EDITION}-java${JAVA_VERSION}-${GRAAL_VERSION}"

    curl -sL https://get.graalvm.org/jdk | bash -s -- $GRAAL_RELEASE \
    --to /tmp \
    -c $GRAAL_COMPONENTS

    local GRAALVM_HOME="/opt/graalvm"
    sudo mv /tmp/$GRAAL_RELEASE $GRAALVM_HOME

    local GRAALSDK_NAME="${GRAAL_VERSION}.${JAVA_VERSION}.grl${GRAAL_EDITION:0:1}"
    sdk install java $GRAALSDK_NAME $GRAALVM_HOME
    sdk default java $GRAALSDK_NAME

    sdk install sbt ${SBT_VERSION}
}

###

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        update-apt-pkgs \
        update-npm-pkgs \
        install-k9s \
        install-tilt \
        install-sdks

set +eax