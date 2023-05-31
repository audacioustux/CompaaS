#!/usr/bin/env bash

set -eax

sudo -iu $_REMOTE_USER <<EOF
    curl -s "https://get.sdkman.io" | bash
EOF
