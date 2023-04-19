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

reset_minikube
start_minikube
set_docker_env
create_buildx_driver
