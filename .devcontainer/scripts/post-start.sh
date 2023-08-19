#!/usr/bin/env bash

set -eax

minikube status -p sdp || minikube start -p sdp
minikube tunnel -p sdp --bind-address "0.0.0.0"
