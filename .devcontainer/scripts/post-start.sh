#!/usr/bin/env bash

set -eax

minikube status || minikube start 
minikube tunnel --bind-address "0.0.0.0" &

tilt up --stream=false
