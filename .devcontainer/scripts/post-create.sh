#!/usr/bin/env bash

set -e
set -a

install-apt-pkgs() {
    sudo apt-get install -y --no-install-recommends \
        wabt \
        binaryen \
        emscripten
}

install-npm-pkgs() {
    npm install -g concurrently
}

cleanup(){
    sudo apt-get autoremove -y
    sudo apt-get clean -y
    git clean -Xdf --exclude='!**/*.env'
}

setup-k8s() {
    # initialize minikube
    minikube delete
    minikube start \
        --driver=docker \
        --cpus=4 \
        --memory=8gb \
        --disk-size=16gb \
        --addons=metrics-server,dashboard

    # use minikube's docker daemon
    echo "eval \$(minikube docker-env)" >> ~/.zshrc

    # create buildx driver
    kubectl create namespace buildkit
    docker buildx create \
        --name=kube \
        --driver=kubernetes \
        --driver-opt=namespace=buildkit,replicas=3,rootless=true

    # set default namespace
    kubectl create ns compaas-dev
    kubectl config set-context --current --namespace=compaas-dev

    # install cert-manager
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml

    # install yugabytedb
    helm repo add yugabytedb https://charts.yugabyte.com
    helm repo update
    helm install yugabyte yugabytedb/yugabyte \
        --version 2.17.3 \
        --set resource.master.requests.cpu=0.5 \
        --set resource.master.requests.memory=0.5Gi \
        --set resource.tserver.requests.cpu=0.5 \
        --set resource.tserver.requests.memory=0.5Gi \
        --set replicas.master=1 \
        --set replicas.tserver=1
}

install-k9s() {
    curl -sS https://webi.sh/k9s | sh
}

install-tilt() {
    curl -fsSL https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
}

install-sdks() {
    curl -s "https://get.sdkman.io" | bash
    source "${SDKMAN_DIR}/bin/sdkman-init.sh"

    sdk env install
}

parallel --halt now,fail=1 --tag --ungroup ::: \
    install-apt-pkgs \
    install-npm-pkgs \
    install-k9s \
    install-tilt \
    install-sdks \
    setup-k8s

cleanup

set +a
set +e