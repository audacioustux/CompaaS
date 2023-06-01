#!/usr/bin/env bash

set -eax

[[ -n "$GRAAL_EE_DOWNLOAD_TOKEN" ]] && EDITION="ee" || EDITION="ce"

RELEASE="graalvm-${EDITION}-java${JAVA}-${VERSION}"

curl -sL https://get.graalvm.org/jdk | bash -s -- $RELEASE \
    --to /tmp \
    -c $COMPONENTS

mv /tmp/$RELEASE $GRAALVM_HOME