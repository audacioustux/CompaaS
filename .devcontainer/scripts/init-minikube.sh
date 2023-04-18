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

reset_minikube
start_minikube
set_docker_env
