FROM mcr.microsoft.com/devcontainers/base:ubuntu

# use geo-local mirror
RUN sed -i.bak -r 's!(deb|deb-src) \S+!\1 mirror://mirrors.ubuntu.com/mirrors.txt!' /etc/apt/sources.list
