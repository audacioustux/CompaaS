FROM ghcr.io/graalvm/graalvm-ce:22

RUN gu install native-image js wasm python

COPY . .

EXPOSE 8080 8558 25520 9099

ENV JAVA_OPTS "-XX:+EagerJVMCI -Dcom.sun.management.jmxremote.port=9099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

ENTRYPOINT bin/compaas