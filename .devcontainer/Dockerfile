FROM mcr.microsoft.com/devcontainers/base:ubuntu

# make `apt-get` commands non-interactive
ENV DEBIAN_FRONTEND=noninteractive

# use geo-local mirrors for apt
RUN sed -i.bak -r 's!(deb|deb-src) \S+!\1 mirror://mirrors.ubuntu.com/mirrors.txt!' /etc/apt/sources.list

# install gnu parallel` for concurrent execution of commands in devcontainer life-cycles
RUN apt-get update && apt-get -y install parallel
