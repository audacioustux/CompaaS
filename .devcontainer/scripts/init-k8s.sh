#!/usr/bin/env bash

set -e

create_cluster(){
    minikube start -p minikube --driver=docker --cpus=3 --memory=8192
}

eval_docker_env(){
    eval $(minikube -p minikube docker-env)

    echo "eval \$(minikube -p minikube docker-env)" >> ~/.zshrc
}

create_cluster
