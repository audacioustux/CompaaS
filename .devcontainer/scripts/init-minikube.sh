#!/usr/bin/env bash

set -e

reset_minikube() {
    minikube delete
}

start_minikube() {
    minikube start --driver=docker --cpus=3 --memory=8gb --disk-size=20gb
}

set_docker_env() {
    echo "eval \$(minikube docker-env)" >> ~/.zshrc
}

reset_minikube
start_minikube
set_docker_env