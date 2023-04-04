#!/usr/bin/env bash

set -e

create_cluster(){
    minikube start -p minikube --driver=docker --cpus=3 --memory=8192
}

create_cluster
