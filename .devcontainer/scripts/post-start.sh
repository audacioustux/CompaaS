#!/usr/bin/env bash

set -eax

enable-swap() {
    LOCATION=/tmp/swapfile
    SIZE=1G

    # Create swapfile
    sudo fallocate -l $SIZE $LOCATION
    sudo chmod 600 $LOCATION
    sudo mkswap $LOCATION
    sudo swapon $LOCATION

    # Enable zswap
    echo 1 | sudo tee /sys/module/zswap/parameters/enabled
}

start-minikube() {
    # Start minikube if it's not already running
    minikube status || minikube start
}

parallel --halt now,fail=1 \
    --linebuffer \
    -j0 ::: \
        enable-swap \
        start-minikube