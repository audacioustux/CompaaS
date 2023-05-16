#!/usr/bin/env bash

set -eax

install-apt-pkgs() {
    sudo apt update
    sudo apt-get install -y --no-install-recommends \
        wabt \
        binaryen \
        emscripten
}

install-npm-pkgs() {
    npm install -g npm@latest
    npm install -g concurrently
}

cleanup(){
    sudo apt-get autoremove -y
    sudo apt-get clean -y

    git clean -Xdf --exclude='!**/*.env'
}

setup-k8s() {
    minikube delete
    minikube start \
        --driver=docker \
        --cpus=4 \
        --memory=8gb \
        --disk-size=16gb \
        --addons=metrics-server \
        --wait=all \
        --wait-timeout=20m \

    echo "eval \$(minikube docker-env)" >> ~/.zshrc

    kubectl create ns compaas-dev
    kubectl config set-context --current --namespace=compaas-dev

    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml

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
    curl https://webi.sh/k9s | sh
}

install-tilt() {
    curl -fL https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
}

install-sdks() {
    curl "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"

    sdk env install
}

###

export DEBIAN_FRONTEND=noninteractive
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
        setup-k8s

cleanup

rm ~/.curlrc ~/.npmrc

set +xae