ARG VARIANT="jammy"
FROM buildpack-deps:${VARIANT}-curl

COPY ./scripts /scripts

ENV DEBIAN_FRONTEND=noninteractive

RUN /scripts/init-common.sh

RUN apt-get install openjdk-17-jdk
