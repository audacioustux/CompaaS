#!/usr/bin/env bash

set -e

install_skaffold() {
    ARCH=$(dpkg --print-architecture)
    cd /tmp
    curl -Lo skaffold https://storage.googleapis.com/skaffold/builds/latest/skaffold-linux-$ARCH && \
    sudo install skaffold /usr/local/bin/
}

install_skaffold