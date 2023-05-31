#!/usr/bin/env bash

set -eax

ARCH=$(uname -m | sed 's/x86_64/amd64/g')
KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt)

curl -LO "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/${ARCH}/kubectl" 
install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl