#!/usr/bin/env bash

set -eax

apt-get update -qq
apt-get install -yqq --no-install-recommends \
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
    parallel 

sudo -iu $_REMOTE_USER <<EOF
    mkdir -p ~/.local/bin
    ln -s $(which batcat) ~/.local/bin/bat
    ln -s $(which fdfind) ~/.local/bin/fd
EOF
