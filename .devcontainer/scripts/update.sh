#!/usr/bin/env bash

set -eax

git clean -Xdf --exclude='!**/*.env'

build-project() {
    sbt stage
}

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        build-project 
