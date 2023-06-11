#!/usr/bin/env bash

set -eax

git clean -Xdf --exclude='!**/*.env'

build-project() {
    sbt stage
}

build-platform() {
    npm install --prefix platform
}

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        build-project \
        build-platform