#!/usr/bin/env bash

set -e

install_pueue() {
    cargo install --locked pueue
}

install_pueue