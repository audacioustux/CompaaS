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

    while IFS= read -r line; do
      candidate="${line%\=*}"
      version="${line#*\=}"
      sdk install "$candidate" "$version"
      sdk default "$candidate" "$version"
    done <".sdkmanrc"

    sdk current
}

###

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        update-apt-pkgs \
        update-npm-pkgs \
        install-sdks

set +eax