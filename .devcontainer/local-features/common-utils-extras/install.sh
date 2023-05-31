#!/usr/bin/env bash

set -eax

export DEBIAN_FRONTEND=noninteractive

apt-get update
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
    socat \
    parallel 