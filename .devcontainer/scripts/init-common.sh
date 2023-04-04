#!/usr/bin/env bash

set -e

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

refresh_apt
install_common-cli-tools
install_wasm_tools
