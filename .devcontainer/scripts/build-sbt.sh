#!/usr/bin/env bash

set -e

publish_local() {
    sbt "Docker / publishLocal"
}

publish_local