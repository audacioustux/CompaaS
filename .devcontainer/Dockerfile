ARG VARIANT="ubuntu-22.04"
FROM mcr.microsoft.com/devcontainers/base:${VARIANT}

ARG USER=vscode
USER $USER

ENV DEBIAN_FRONTEND="noninteractive"
RUN sudo apt update
RUN sudo apt upgrade -y
RUN sudo apt install -y --no-install-recommends \
    ripgrep \
    neofetch \
    build-essential \
    libz-dev \
    zlib1g-dev \
    binaryen \
    wabt

### Install GraalVM
ARG GRAALVM_VERSION=java17-22.3.0
RUN curl -sL https://get.graalvm.org/jdk \
    | sudo bash -s -- graalvm-ce-${GRAALVM_VERSION} \
    -c native-image,wasm,nodejs

ENV GRAALVM_HOME "${HOME}/graalvm-ce-${GRAALVM_VERSION}"
ENV PATH "${GRAALVM_HOME}/bin:$PATH"
ENV JAVA_HOME "${GRAALVM_HOME}"

ARG WORKSPACES_DIR="/workspaces"
WORKDIR ${WORKSPACES_DIR}
RUN sudo chown $USER $WORKSPACES_DIR
RUN sudo chmod 755 $WORKSPACES_DIR

RUN curl -s "https://get.sdkman.io" | bash
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install sbt"