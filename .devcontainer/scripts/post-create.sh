#!/usr/bin/env bash

set -eax

setup-k8s() {
    k3d cluster create --config k3d-dev.yaml
}

add-helm-repos() {
    helm repo add cnpg https://cloudnative-pg.github.io/charts
    helm repo add jetstack https://charts.jetstack.io
    helm repo add kubevela https://charts.kubevela.net/core
    helm repo update
}

install-cert-manager() {
    helm install cert-manager jetstack/cert-manager \
        --namespace cert-manager \
        --create-namespace \
        --set installCRDs=true
}

install-postgres() {
    helm install cnpg cnpg/cloudnative-pg \
        --namespace cnpg-system \
        --create-namespace
}

install-kubevla() {
    helm install kubevela kubevela/vela-core \
        --namespace vela-system \
        --create-namespace
}

cleanup() {
    git clean -Xdf --exclude='!**/*.env'
}

###

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        setup-k8s \
        add-helm-repos \
        cleanup

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        install-cert-manager \
        install-postgres \
        install-kubevela
