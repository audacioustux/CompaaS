#!/usr/bin/env bash

set -eax

build-project() {
    source ~/.sdkman/bin/sdkman-init.sh

    sbt stage
}

###

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        build-project

set +eax