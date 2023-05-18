#!/usr/bin/env bash

set -eax

setup-k8s() {
    minikube delete
    minikube start \
        --driver=docker \
        --cpus=4 \
        --memory=8gb \
        --disk-size=16gb \
        --addons=metrics-server 

    echo "eval \$(minikube docker-env)" >> ~/.zshrc

    kubectl create ns compaas-dev
    kubectl config set-context --current --namespace=compaas-dev
}

install-cert-manager() {
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml
}

install-yugabyte() {
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

###

setup-k8s

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        install-cert-manager \
        install-yugabyte

git clean -Xdf --exclude='!**/*.env'
rm ~/.curlrc ~/.npmrc

set +eax