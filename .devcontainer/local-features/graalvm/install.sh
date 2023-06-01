#!/usr/bin/env bash

set -eax

[[ -n "$GRAAL_EE_DOWNLOAD_TOKEN" ]] && GRAAL_EDITION="ee" || GRAAL_EDITION="ce"
: ${JAVA_VERSION:=17}
: ${GRAAL_VERSION:=22.3.1}
: ${GRAAL_COMPONENTS:="native-image,js,wasm"}

GRAAL_RELEASE="graalvm-${GRAAL_EDITION}-java${JAVA_VERSION}-${GRAAL_VERSION}"
echo "Installing GraalVM ${GRAAL_RELEASE}..."

curl -sL https://get.graalvm.org/jdk | bash -s -- $GRAAL_RELEASE \
    --to /opt \
    -c $GRAAL_COMPONENTS

ln -s /opt/$GRAAL_RELEASE $JAVA_HOME