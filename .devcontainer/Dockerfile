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
    -c native-image,wasm,nodejs \
    --to ${HOME}

ENV GRAALVM_HOME="${HOME}/graalvm-ce-${GRAALVM_VERSION}"
ENV PATH="${HOME}/graalvm-ce-${GRAALVM_VERSION}/bin:$PATH"
ENV JAVA_HOME="${HOME}/graalvm-ce-${GRAALVM_VERSION}"

### Initialize Workspace
WORKDIR /workspace
RUN chown vscode:vscode /workspace
RUN chmod 755 /workspace

USER vscode
RUN git clone https://github.com/audacioustux/CompaaS.git .
