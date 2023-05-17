FROM mcr.microsoft.com/devcontainers/base:ubuntu

# use geo-local mirror
RUN sed -i.bak -r 's!(deb|deb-src) \S+!\1 mirror://mirrors.ubuntu.com/mirrors.txt!' /etc/apt/sources.list

ENV DEBIAN_FRONTEND=noninteractive
RUN sudo apt-get update && sudo apt-get install -y --no-install-recommends \
    ripgrep \
    bat \
    exa \
    delta \
    hyperfine \
    fd-find \
    zoxide \
    neovim \
    python3-neovim \
    httpie \
    fzf \
    socat \
    ncdu \
    parallel \
    openjdk-17-jdk-headless
