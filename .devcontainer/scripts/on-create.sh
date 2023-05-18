#!/usr/bin/env bash

set -eax

install-apt-pkgs() {
    sudo apt-get update
    sudo apt-get install -y --no-install-recommends \
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
        socat \
        ncdu \
        parallel \
        wabt \
        binaryen \
        emscripten
}

install-npm-pkgs() {
    npm install -g concurrently
}

install-sdkman() {
    curl "https://get.sdkman.io" | bash
}

###

echo "-sS" > ~/.curlrc
echo "progress=false" > ~/.npmrc

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        install-apt-pkgs \
        install-npm-pkgs \
        install-sdkman

set +eax