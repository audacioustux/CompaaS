ARG VARIANT="ubuntu-22.04"
FROM mcr.microsoft.com/devcontainers/base:${VARIANT}

ENV DEBIAN_FRONTEND="noninteractive"
RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install -y --no-install-recommends \
    ripgrep \
    neofetch \
    build-essential \
    libz-dev \
    zlib1g-dev

### Install GraalVM
ARG GRAALVM_VERSION=java17-22.3.0
RUN curl -sL https://get.graalvm.org/jdk \
    | bash -s -- graalvm-ce-${GRAALVM_VERSION} \
    -c native-image,wasm,nodejs

ENV GRAALVM_HOME="${HOME}/graalvm-ce-${GRAALVM_VERSION}"
ENV PATH="${HOME}/graalvm-ce-${GRAALVM_VERSION}/bin:$PATH"
ENV JAVA_HOME="${HOME}/graalvm-ce-${GRAALVM_VERSION}"

### Initialize Workspace
ARG WORKSPACE_DIR="/workspace"
WORKDIR ${WORKSPACE_DIR}
RUN chown vscode $WORKSPACE_DIR
RUN chmod 755 $WORKSPACE_DIR

USER vscode
