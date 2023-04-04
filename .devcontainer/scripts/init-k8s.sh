#!/usr/bin/env bash

set -e

create_cluster(){
    minikube start
}

init_docker_env(){
    echo "eval \$(minikube docker-env)" >> ~/.zshrc
}

install_k9s() {
    curl -sS https://webi.sh/k9s | sh
}

create_cluster
init_docker_env
install_k9s