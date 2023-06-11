#!/usr/bin/env bash

set -eax

if [ "$(uname -m)" = "aarch64" ]; then
    curl -fL "https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-pc-linux.gz" | gzip -d > cs
elif [ "$(uname -m)" = "x86_64" ]; then
    curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > cs
else
    echo "Unsupported architecture: $(uname -m)"
    exit 1
fi

chmod +x cs

sudo -iu $_REMOTE_USER <<EOF
    ./cs setup
EOF