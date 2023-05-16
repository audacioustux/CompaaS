#!/usr/bin/env bash

set -eax

minikube status || minikube start

set +xae