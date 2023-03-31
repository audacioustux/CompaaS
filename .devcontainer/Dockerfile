ARG VARIANT="ubuntu-22.04"
FROM mcr.microsoft.com/devcontainers/base:${VARIANT}

RUN chsh -s $(which zsh)

SHELL ["zsh", "-c"]

ARG DEBIAN_FRONTEND="noninteractive"
RUN apt update
RUN apt upgrade -y
RUN apt install -y --no-install-recommends \
    ripgrep \
    neofetch \
    build-essential \
    libz-dev \
    zlib1g-dev \
    binaryen \
    wabt \ 
    lldb \
    clang

### Install Rust
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
RUN rustup toolchain add nightly
RUN rustup target add wasm32-wasi --toolchain nightly
RUN rustup target add wasm32-unknown-unknown --toolchain nightly
RUN rustup default nightly

### Install SDKMAN
RUN curl -s "https://get.sdkman.io" | bash

### Install GraalVM
ARG GRAALVM_EDITION=ce
ARG GRAAL_EE_DOWNLOAD_TOKEN=
ARG GRAALVM_VERSION=22.3.1
ARG JAVA_VERSION=java17
ENV GRAALVM_NAME=graalvm-${GRAALVM_EDITION}-${JAVA_VERSION}-${GRAALVM_VERSION}

RUN curl -sL https://get.graalvm.org/jdk | bash -s -- $GRAALVM_NAME \
    --to /opt \
    -c native-image,nodejs,wasm

ENV GRAALVM_HOME="/opt/${GRAALVM_NAME}"
RUN source ~/.zshrc && sdk install java ${GRAALVM_VERSION:0:6}.r${JAVA_VERSION:4:2}-grl${GRAALVM_EDITION:0:1} ${GRAALVM_HOME}

ENV PATH "$PATH:${GRAALVM_HOME}/bin"
ENV JAVA_HOME "${GRAALVM_HOME}"

### Install Scala and SBT
RUN source ~/.zshrc && sdk install sbt

### Install Emscripten
#### NOTE: https://github.com/nodejs/docker-node/issues/1559
RUN apt install -y emscripten

### Install NVM
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash

### Install nodejs
RUN source ~/.zshrc && nvm install v18.13.0

### Install common utilities
RUN source ~/.zshrc && npm install -g \
    js-yaml

ARG WORKSPACES_DIR="/workspaces"
WORKDIR ${WORKSPACES_DIR}
