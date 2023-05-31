#!/usr/bin/env bash

set -eax

if [ -n "${GRAAL_EE_DOWNLOAD_TOKEN}" ]; then
    GRAAL_EDITION="ee"
else
    GRAAL_EDITION="ce"
fi
GRAAL_VERSION="22.3.1"
JAVA_VERSION="17"
GRAAL_COMPONENTS="native-image,js,wasm,llvm-toolchain,espresso,python"

GRAAL_RELEASE="graalvm-${GRAAL_EDITION}-java${JAVA_VERSION}-${GRAAL_VERSION}"

curl -sL https://get.graalvm.org/jdk | bash -s -- $GRAAL_RELEASE \
    --to /tmp \
    -c $GRAAL_COMPONENTS

mv /tmp/$GRAAL_RELEASE $GRAALVM_HOME
