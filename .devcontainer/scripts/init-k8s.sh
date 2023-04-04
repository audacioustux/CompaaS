#!/usr/bin/env bash

set -e

create_cluster(){
    minikube start
}

install_k9s() {
    curl -sS https://webi.sh/k9s | sh
}

create_cluster
install_k9s
