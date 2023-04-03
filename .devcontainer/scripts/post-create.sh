#!/usr/bin/env bash

ARCH=$(uname -m)
case "$ARCH" in
  arm64|aarch64)
    ARCHITECTURE="arm64"
    ;;
  x86_64|amd64)
    ARCHITECTURE="amd64"
    ;;
  *)
    echo "Unknown Architecture: $ARCH"
    exit 1
    ;;
esac

USERHOME="/home/$USERNAME"
if [ "$USERNAME" = "root" ]; then
    USERHOME="/root"
fi

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

install_kubectl() {
    KUBECTL_VERSION="$(curl -sSL https://dl.k8s.io/release/stable.txt)"
    sudo curl -sSL -o /usr/local/bin/kubectl "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/${ARCHITECTURE}/kubectl"
    sudo chmod 0755 /usr/local/bin/kubectl

    # Verify kubectl binary checksum
    KUBECTL_SHA256="$(curl -sSL "https://dl.k8s.io/${KUBECTL_VERSION}/bin/linux/${ARCHITECTURE}/kubectl.sha256")"
    echo "${KUBECTL_SHA256} */usr/local/bin/kubectl" | sha256sum -c -
    if ! type kubectl > /dev/null 2>&1; then
        echo '(!) kubectl installation failed!'
        exit 1
    fi

    # kubectl bash completion
    kubectl completion bash > /etc/bash_completion.d/kubectl

    # kubectl zsh completion
    if [ -e "${USERHOME}}/.oh-my-zsh" ]; then
        mkdir -p "${USERHOME}/.oh-my-zsh/completions"
        kubectl completion zsh > "${USERHOME}/.oh-my-zsh/completions/_kubectl"
        chown -R "${USERNAME}" "${USERHOME}/.oh-my-zsh"
    fi
}

install_minikube() {
    MINIKUBE_VERSION="latest"
    sudo curl -sSL -o /usr/local/bin/minikube "https://storage.googleapis.com/minikube/releases/${MINIKUBE_VERSION}/minikube-linux-${ARCHITECTURE}"    
    sudo chmod 0755 /usr/local/bin/minikube

    # Verify minikube binary checksum
    MINIKUBE_SHA256="$(curl -sSL "https://storage.googleapis.com/minikube/releases/${MINIKUBE_VERSION}/minikube-linux-${ARCHITECTURE}.sha256")"
    echo "${MINIKUBE_SHA256} */usr/local/bin/minikube" | sha256sum -c -
    if ! type minikube > /dev/null 2>&1; then
        echo '(!) minikube installation failed!'
        exit 1
    fi

    # Create minkube folder with correct privs in case a volume is mounted here
    mkdir -p "${USERHOME}/.minikube"
    chown -R $USERNAME "${USERHOME}/.minikube"
    chmod -R u+wrx "${USERHOME}/.minikube"
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

cleanup() {
    if [ -n "$(command -v apt-get)" ]; then
        apt-get autoremove -y
        apt-get clean -y
    elif [ -n "$(command -v apk)" ]; then
        apk del --purge .build-deps
        rm -rf /var/cache/apk/*
    elif [ -n "$(command -v yum)" ]; then
        yum autoremove -y
        yum clean all
    fi
}

install_sdkman
install_graalvm
install_sbt
install_kubectl
install_minikube
create_k8s_cluster
minikube_docker_env
publishLocalImage
apply_k8s_manifests
install_k9s
