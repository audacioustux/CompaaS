#!/usr/bin/env zsh

install_sdkman() {
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk version
}

install_graalvm() {
    GRAALVM_EDITION=${GRAALVM_EDITION:-ce}
    GRAALVM_VERSION=${GRAALVM_VERSION:-"22.3.1"}
    GRAALVM_COMPONENTS=${GRAALVM_COMPONENTS:-"native-image,nodejs,wasm"}
    JAVA_VERSION=${JAVA_VERSION:-"java17"}

    GRAALVM_NAME=graalvm-${GRAALVM_EDITION}-${JAVA_VERSION}-${GRAALVM_VERSION}

    curl -sL https://get.graalvm.org/jdk | sudo bash -s -- $GRAALVM_NAME \
        --to /opt \
        -c $GRAALVM_COMPONENTS

    GRAALVM_HOME="/opt/${GRAALVM_NAME}"

    SDK_GRAALVM_ID=${GRAALVM_VERSION:0:6}.r${JAVA_VERSION:4:2}-grl${GRAALVM_EDITION:0:1}
    sdk install java ${SDK_GRAALVM_ID} ${GRAALVM_HOME}
    sdk default java ${SDK_GRAALVM_ID}

    sdk current java
}

install_sbt() {
    sdk install sbt

    sdk current sbt
}

create_k8s_cluster() {
    minikube start --cpus 3 --memory 6144 --driver=docker
}

minikube_docker_env() {
    eval $(minikube docker-env)
}

publishLocalImage() {
    sbt docker:publishLocal
}

apply_k8s_manifests() {
    kubectl apply -k k8s/overlays/minikube
}

install_k9s() {
    curl -sS https://webi.sh/k9s | sh
}

install_sdkman
install_graalvm
install_sbt
create_k8s_cluster
minikube_docker_env
publishLocalImage
apply_k8s_manifests
install_k9s
