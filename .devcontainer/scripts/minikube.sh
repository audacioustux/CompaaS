#!/usr/bin/env bash

set -e

create_cluster(){
    minikube start --cpus 2 --memory 4096 --driver=docker
}

init_docker_env(){
    echo "eval \$(minikube docker-env)" >> ~/.zshrc
}

create_cluster
init_docker_env