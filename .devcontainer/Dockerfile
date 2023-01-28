ARG VARIANT="ubuntu-22.04"
FROM mcr.microsoft.com/devcontainers/base:${VARIANT}

WORKDIR $HOME

ENV DEBIAN_FRONTEND="noninteractive"
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

### Install GraalVM
ARG GRAALVM_VERSION=java17-22.3.0
RUN curl -sL https://get.graalvm.org/jdk \
    | sudo bash -s -- graalvm-ce-${GRAALVM_VERSION} \
    -c native-image,wasm,nodejs

ENV GRAALVM_HOME "${HOME}/graalvm-ce-${GRAALVM_VERSION}"
ENV PATH "${GRAALVM_HOME}/bin:$PATH"
ENV JAVA_HOME "${GRAALVM_HOME}"

### Install Scala and SBT
RUN curl -s "https://get.sdkman.io" | bash
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install sbt"

### Install Rust
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
RUN rustup toolchain add nightly
RUN rustup target add wasm32-wasi --toolchain nightly
RUN rustup target add wasm32-unknown-unknown --toolchain nightly
RUN rustup default nightly

ARG WORKSPACES_DIR="/workspaces"
WORKDIR ${WORKSPACES_DIR}
