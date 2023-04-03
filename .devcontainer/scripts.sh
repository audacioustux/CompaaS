#!/usr/bin/env bash

install_sdkman() {
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
}

install_graalvm() {
    GRAALVM_EDITION=${GRAALVM_EDITION:-ce}
    GRAALVM_VERSION=${GRAALVM_VERSION:-"22.3.1"}
    JAVA_VERSION=${JAVA_VERSION:-"java17"}

    GRAALVM_NAME=graalvm-${GRAALVM_EDITION}-${JAVA_VERSION}-${GRAALVM_VERSION}

    curl -sL https://get.graalvm.org/jdk | sudo bash -s -- $GRAALVM_NAME \
        --to $HOME/ 

    GRAALVM_HOME="$HOME/${GRAALVM_NAME}"

    SDK_GRAALVM_ID=${GRAALVM_VERSION:0:6}.r${JAVA_VERSION:4:2}-grl${GRAALVM_EDITION:0:1}
    sdk install java ${SDK_GRAALVM_ID} ${GRAALVM_HOME}
    sdk default java ${SDK_GRAALVM_ID}

    export PATH=${GRAALVM_HOME}/bin:$PATH
    export JAVA_HOME=${GRAALVM_HOME}
}

install_graalvm_components(){
    GRAALVM_COMPONENTS=${GRAALVM_COMPONENTS:-"native-image,nodejs,wasm"}
    for component in ${GRAALVM_COMPONENTS//,/ }; do
        gu install ${component}
    done
}

install_sbt() {
    sdk install sbt
}

create_minikube_cluster() {
    minikube start --cpus 2 --memory 4096 --driver=docker
}

minikube_docker_env() {
    echo "eval \$(minikube docker-env)" >> ~/.zshrc
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
