#!/usr/bin/env bash

set -e

install_concurrently() {
    npm install -g concurrently
}

install_concurrently