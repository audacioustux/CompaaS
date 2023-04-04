#!/usr/bin/env bash

set -e

remove_trace() {
    minikube delete
}

start_minikube() {
    minikube start --driver=docker --cpus=3 --memory=6144 --disk-size=16g
}

set_minikube_docker_env() {
    echo "eval \$(minikube docker-env)" >> ~/.zshrc
}

remove_trace
start_minikube
set_minikube_docker_env
