#!/usr/bin/env bash

set -e

install_tilt() {
    curl -fsSL https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
}

install_tilt