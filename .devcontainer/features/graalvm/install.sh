#!/usr/bin/env bash

set -eax

URL_BASE=https://download.oracle.com/graalvm/${JAVA}/${VERSION}
JDK_HOME=/usr/java/graal-${VERSION}-jdk${JAVA}

ARCH="$(uname -m)"
if [ "$ARCH" = "x86_64" ]; then
    ARCH="x64"
fi

JAVA_PKG="$URL_BASE"/graalvm-jdk-${JAVA}_linux-"${ARCH}"_bin.tar.gz
JAVA_SHA256=$(curl "$JAVA_PKG".sha256) 
curl --output /tmp/jdk.tgz "$JAVA_PKG"
echo "$JAVA_SHA256 */tmp/jdk.tgz" | sha256sum -c
mkdir -p "$JDK_HOME"
tar --extract --file /tmp/jdk.tgz --directory "$JDK_HOME" --strip-components 1

mkdir -p "$JAVA_HOME"
ln -s $JDK_HOME $JAVA_HOME

if [[ -n "$COMPONENTS" ]]; then
    gu install $COMPONENTS
fi
