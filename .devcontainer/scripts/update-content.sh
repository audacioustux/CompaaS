#!/usr/bin/env bash

set -eax

update-apt-pkgs() {
    sudo apt-get update
    sudo apt-get upgrade -y
}

update-npm-pkgs() {
    npm update -g
}

install-sdks() {
    source ~/.sdkman/bin/sdkman-init.sh

    sdk env install
}

cleanup(){
    sudo apt-get autoremove -y
    sudo apt-get clean -y
}

###

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        update-apt-pkgs \
        update-npm-pkgs \
        install-sdks

cleanup

set +eax