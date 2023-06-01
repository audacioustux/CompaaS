#!/usr/bin/env bash

set -eax

apt-get update
apt-get install -y --no-install-recommends \
    wabt \
    binaryen \
    emscripten