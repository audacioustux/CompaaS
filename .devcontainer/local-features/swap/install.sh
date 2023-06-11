#!/usr/bin/env bash

set -eax

LOCATION=/tmp/swapfile
SIZE=1G

fallocate -l $SIZE $LOCATION
chmod 600 $LOCATION
mkswap $LOCATION
swapon $LOCATION
echo "$LOCATION none swap sw 0 0" | tee -a /etc/fstab
