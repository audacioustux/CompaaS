#!/usr/bin/env bash

set -eax

install-apt-pkgs() {
    sudo apt-get update
    sudo apt-get install -y --no-install-recommends \
        ripgrep \
        bat \
        exa \
        delta \
        hyperfine \
        fd-find \
        zoxide \
        neovim \
        python3-neovim \
        httpie \
        fzf \
        socat \
        parallel \
        wabt \
        binaryen \
        emscripten
}

install-npm-pkgs() {
    npm install -g concurrently
}

install-k9s() {
    curl https://webi.sh/k9s | sh
}

install-tilt() {
    curl https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
}

install-k3d() {
    curl -s https://raw.githubusercontent.com/k3d-io/k3d/main/install.sh | bash
}

install-kubectl() {
    local ARCH=$(uname -m | sed 's/x86_64/amd64/g')
    local KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt)

    curl -LO "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/${ARCH}/kubectl"
    sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
    rm kubectl
}

install-helm() {
    curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
}

install-sdks() {
    curl "https://get.sdkman.io" | bash
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

echo "-sS" > ~/.curlrc
echo "progress=false" > ~/.npmrc

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        install-apt-pkgs \
        install-npm-pkgs \
        install-k9s \
        install-tilt \
        install-sdks \
        install-kubectl \
        install-k3d \
        install-helm

set +eax