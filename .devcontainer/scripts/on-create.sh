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
    sudo apt-get clean
}

install-npm-pkgs() {
    npm install -g concurrently
}

install-sdkman() {
    curl "https://get.sdkman.io" | bash

    sed -i 's/sdkman_auto_env=false/sdkman_auto_env=true/g' ~/.sdkman/etc/config
}

install-k9s() {
    curl https://webi.sh/k9s | sh
}

install-tilt() {
    curl https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
}

###

echo "-sS" > ~/.curlrc
echo "progress=false" > ~/.npmrc

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        install-apt-pkgs \
        install-npm-pkgs \
        install-sdkman \
        install-k9s \
        install-tilt

set +eax