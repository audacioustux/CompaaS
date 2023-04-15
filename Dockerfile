FROM ghcr.io/graalvm/graalvm-ce:22

RUN gu install native-image js wasm python

COPY . .

EXPOSE 8080 8558 25520

ENV JAVA_OPTS "-XX:+EagerJVMCI"

ENTRYPOINT bin/compaas