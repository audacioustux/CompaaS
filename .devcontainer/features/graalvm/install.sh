#!/usr/bin/env bash

set -eax

JAVA_URL=https://download.oracle.com/java/${JAVA}/${VERSION}
JAVA_HOME=/usr/java/jdk-${JAVA}

ARCH="$(uname -m)"
if [ "$ARCH" = "x86_64" ]; then
    ARCH="x64"
fi

JAVA_PKG="$JAVA_URL"/jdk-${JAVA}-"${ARCH}"_bin.tar.gz
JAVA_SHA256=$(curl "$JAVA_PKG".sha256) 
curl --output /tmp/jdk.tgz "$JAVA_PKG"
echo "$JAVA_SHA256 */tmp/jdk.tgz" | sha256sum -c
mkdir -p "$JAVA_HOME"
tar --extract --file /tmp/jdk.tgz --directory "$JAVA_HOME" --strip-components 1

PATH=$JAVA_HOME/bin:$PATH

if [[ -n "$COMPONENTS" ]]; then
    gu install $COMPONENTS
fi

mkdir -p "$GRAALVM_HOME"
ln -s $JAVA_HOME $GRAALVM_HOME
