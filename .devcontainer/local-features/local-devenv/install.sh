#!/usr/bin/env bash

set -eax

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
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
    rm kubectl
}

install-helm() {
    curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
}

install-apt-pkgs() {
    apt-get install -y --no-install-recommends \
        wabt \
        binaryen \
        emscripten
}

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        install-tilt \
        install-k3d \
        install-kubectl \
        install-helm \
        install-apt-pkgs
