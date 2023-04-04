#!/usr/bin/env bash

set -e

create_cluster(){
    minikube start -p minikube
}

set_docker_env(){
    echo "eval \$(minikube -p minikube docker-env)" >> ~/.zshrc
}

create_cluster
set_docker_env
