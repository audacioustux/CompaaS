#!/usr/bin/env bash

set -e

start_minikube(){
    minikube start -p minikube
}

start_minikube