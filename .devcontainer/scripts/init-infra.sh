#!/usr/bin/env bash

set -e

reset_minikube() {
    minikube delete
}

start_minikube() {
    minikube start \
        --driver=docker \
        --cpus=4 \
        --memory=8gb \
        --disk-size=16gb \
        --addons=metrics-server,dashboard
}

set_docker_env() {
    echo "eval \$(minikube docker-env)" >> ~/.zshrc
}

create_buildx_driver() {
    kubectl create namespace buildkit
    docker buildx create \
        --name=kube \
        --driver=kubernetes \
        --driver-opt=namespace=buildkit,replicas=3,rootless=true
}

set_default_namespace() {
    kubectl create ns compaas-dev
    kubectl config set-context --current --namespace=compaas-dev
}

install_cert_manager() {
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml
}

install_yugabytedb() {
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

reset_minikube
start_minikube
set_docker_env
create_buildx_driver
set_default_namespace
install_cert_manager
install_yugabytedb
