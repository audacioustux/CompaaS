#!/usr/bin/env bash

set -e

refresh_apt(){
    apt-get update
    apt-get upgrade -y
}

install_common-cli-tools(){
    apt-get install -y --no-install-recommends \
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
    apt-get install -y --no-install-recommends \
        wabt \
        binaryen \
        emscripten
}

refresh_apt
install_common-cli-tools
install_wasm_tools
