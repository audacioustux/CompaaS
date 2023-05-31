#!/usr/bin/env bash

set -eax

if [ -n "${GRAAL_EE_DOWNLOAD_TOKEN}" ]; then
    EDITION="ee"
else
    EDITION="ce"
fi
JAVA_VERSION="17"

RELEASE="graalvm-${EDITION}-java${JAVA_VERSION}-${VERSION}"

curl -sL https://get.graalvm.org/jdk | bash -s -- $RELEASE \
    --to /opt \
    -c $COMPONENTS

ln -s /opt/$RELEASE /opt/graalvm