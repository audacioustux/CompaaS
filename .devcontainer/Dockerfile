FROM mcr.microsoft.com/devcontainers/base:ubuntu

# make `apt-get` commands non-interactive
ENV DEBIAN_FRONTEND=noninteractive

# use geo-local mirrors for apt
RUN sed -i.bak -r 's!(deb|deb-src) \S+!\1 mirror://mirrors.ubuntu.com/mirrors.txt!' /etc/apt/sources.list

# install gnu parallel` for concurrent execution of commands in devcontainer life-cycles
RUN apt-get update && apt-get -y install parallel

# install graalvm
ARG GRAAL_ID="graalvm-${GRAAL_EDITION:-ce}-java${JAVA_VERSION:-17}-${GRAAL_VERSION:-22.3.1}"
RUN curl -sL https://get.graalvm.org/jdk | bash -s -- ${GRAAL_ID} \
    -c native-image,llvm-toolchain,wasm,js,espresso,python \
    --no-progress \
    --to "/opt"

ENV GRAALVM_HOME="/opt/${GRAAL_ID}"
ENV JAVA_HOME="${GRAALVM_HOME}"
ENV PATH="${GRAALVM_HOME}/bin:${PATH}"
