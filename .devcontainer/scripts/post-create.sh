#!/usr/bin/env bash

set -eax

# clean up any untracked files
minikube delete -p sdp
# start minikube
minikube start --cpus 4 --memory 6g --driver=docker --cni=false -p sdp
# install ciliium
cilium install
# enable hubble
cilium hubble enable
# use minikube's docker daemon
echo "eval \$(minikube docker-env)" >> ~/.zshrc