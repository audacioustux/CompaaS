#!/usr/bin/env bash

set -eax

git clean -Xdf --exclude='!**/*.env'

build-project() {
    sbt stage
}

install-platform-deps() {
    npm install --prefix platform
}

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        build-project \
        install-platform-deps
