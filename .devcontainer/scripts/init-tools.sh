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
        tldr \
        httpie \
        ncdu 
}

install_wasm_tools() {
    sudo apt-get install -y --no-install-recommends \
        wabt \
        binaryen \
        emscripten
}

install_k9s() {
    curl -sS https://webi.sh/k9s | sh
}

refresh_apt
install_common-cli-tools
install_wasm_tools
install_k9s
