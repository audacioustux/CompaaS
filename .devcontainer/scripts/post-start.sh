#!/usr/bin/env bash

set -eax

enable-swap() {
    LOCATION=/tmp/swapfile
    SIZE=1G

    fallocate -l $SIZE $LOCATION
    chmod 600 $LOCATION
    mkswap $LOCATION
    swapon $LOCATION

    echo 1 > /sys/module/zswap/parameters/enabled
}

start-minikube() {
    minikube status || minikube start
}

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        enable-swap \
        start-minikube