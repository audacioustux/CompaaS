#!/usr/bin/env bash

set -eax

minikube status || minikube start 

tunnel-minikube() {
  minikube tunnel --bind-address "0.0.0.0"
}

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        "tunnel-minikube" 

