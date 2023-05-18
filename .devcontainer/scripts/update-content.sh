#!/usr/bin/env bash

set -eax

update-apt-pkgs() {
    sudo apt-get update
    sudo apt-get upgrade -y
}

update-npm-pkgs() {
    npm update -g
}

install-k9s() {
    curl https://webi.sh/k9s | sh
}

install-tilt() {
    curl https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
}

install-sbt() {
    source ~/.sdkman/bin/sdkman-init.sh

    sdk install sbt
}

###

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        update-apt-pkgs \
        update-npm-pkgs \
        install-k9s \
        install-tilt \
        install-sbt

set +eax