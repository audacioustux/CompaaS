#!/usr/bin/env bash

set -eax

update-apt-pkgs() {
    sudo apt-get update
    sudo apt-get upgrade -y
    sudo apt-get clean
}

update-npm-pkgs() {
    npm update -g
}

install-sdks() {
    source ~/.sdkman/bin/sdkman-init.sh

    sdk env install
}

###

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        update-apt-pkgs \
        update-npm-pkgs \
        install-sdks

set +eax