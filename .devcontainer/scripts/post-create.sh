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

    SDK_GRAALVM_ID=${GRAALVM_VERSION:0:6}.r${JAVA_VERSION:4:2}-grl${GRAALVM_EDITION:0:1}
    sdk install java ${SDK_GRAALVM_ID} ${GRAALVM_HOME}
    sdk default java ${SDK_GRAALVM_ID}

    java -version
}

install_sbt() {
    sdk install sbt
}

create_k8s_cluster() {
    minikube start --cpus 4 --memory 8192 --driver=docker
}

apply_k8s_manifests() {
    kubectl apply -f k8s/manifests
}

install_sdkman
install_graalvm
install_sbt
create_k8s_cluster
apply_k8s_manifests
