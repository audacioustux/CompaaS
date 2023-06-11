#!/usr/bin/env bash

set -eax

# clean up any untracked files
minikube delete
# start minikube with all available resources
minikube start \
    --cpus=$(nproc) \
    --memory=$(free -m | awk '/^Mem:/{print $2}')
# use minikube's docker daemon
echo "eval \$(minikube docker-env)" >> ~/.zshrc

# if no backend url is specified
if [[ -z "$PULUMI_BACKEND_URL" ]]; then
    # pulumi cloud is the default, but if no access token is specified
    if [[ -z "$PULUMI_ACCESS_TOKEN" ]]; then
        # use local backend
        export PULUMI_BACKEND_URL="--local"
        # if no passphrase is specified
        if [[ -z "$PULUMI_CONFIG_PASSPHRASE" ]]; then
            # use default passphrase
            export PULUMI_CONFIG_PASSPHRASE="pulumi"
        fi
    fi
fi

pulumi login

# set default org if specified
if [[ -n "$PULUMI_DEFAULT_ORG" ]]; then
    pulumi org set-default $PULUMI_DEFAULT_ORG
fi

# set stack name to dev if not set
if [[ -z "$PULUMI_STACK_NAME" ]]; then
    export PULUMI_STACK_NAME="devcontainer"
fi

pulumi stack select -s $PULUMI_STACK_NAME --create -C platform
pulumi up -y -r -s $PULUMI_STACK_NAME --suppress-outputs -C platform
