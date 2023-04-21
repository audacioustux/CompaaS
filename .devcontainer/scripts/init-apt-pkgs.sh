#!/usr/bin/env bash

set -e

export DEBIAN_FRONTEND=noninteractive

refresh_apt(){
    sudo apt-get update
    sudo apt-get upgrade -y
}

install_common-cli-tools(){
    sudo apt-get install -y --no-install-recommends \
        neovim \
        python3-neovim \
        fzf \
        bat \
        ripgrep \
        exa \
        httpie \
        ncdu \
        socat \
        parallel
}

install_wasm_tools() {
    sudo apt-get install -y --no-install-recommends \
        wabt \
        binaryen \
        emscripten
}

install_postgres_client() {
    sudo apt-get install -y --no-install-recommends \
        postgresql-client
}

cleanup_apt(){
    sudo apt-get autoremove -y
    sudo apt-get clean -y
}

refresh_apt
install_common-cli-tools
install_wasm_tools
install_postgres_client