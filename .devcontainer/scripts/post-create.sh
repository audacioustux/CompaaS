#!/usr/bin/env bash

set -eax

k3d cluster delete --all
git clean -Xdf --exclude='!**/*.env'

k3d cluster create --config k3d-dev.yaml

pulumi up --yes --stack dev --suppress-outputs --cwd platform
