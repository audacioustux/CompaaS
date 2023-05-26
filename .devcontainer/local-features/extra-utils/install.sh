#!/usr/bin/env bash

set -eax

install-parallel() {
    apt-get install -y --no-install-recommends parallel
}

install-apt-pkgs() {
    apt-get install -y --no-install-recommends \
        ripgrep \
        bat \
        exa \
        delta \
        hyperfine \
        fd-find \
        zoxide \
        neovim \
        python3-neovim \
        httpie \
        fzf \
        socat 
}

install-npm-pkgs() {
    npm install -g concurrently
}

apt-get update
install-parallel

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        install-apt-pkgs \
        install-npm-pkgs