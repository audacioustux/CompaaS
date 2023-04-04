#!/usr/bin/env bash

set -e

publish_local() {
    eval $(minikube -p minikube docker-env)

    sbt "Docker / publishLocal"
}

publish_local