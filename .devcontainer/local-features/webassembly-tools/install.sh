#!/usr/bin/env bash

set -eax

export DEBIAN_FRONTEND=noninteractive

apt-get update
apt-get install -y --no-install-recommends \
    wabt \
    binaryen \
    emscripten