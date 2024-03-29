FROM ghcr.io/graalvm/graalvm-community:20

RUN gu install native-image js wasm

RUN curl -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar \
    -o opentelemetry-javaagent.jar

COPY . .

EXPOSE 8080 8558 25520 9010 9020

ENV JAVA_OPTS \
    -XX:+EagerJVMCI \
    # -javaagent:opentelemetry-javaagent.jar \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=9010 \
    -Dcom.sun.management.jmxremote.rmi.port=9010 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Djava.rmi.server.hostname=0.0.0.0


ENTRYPOINT bin/compaas -jvm-debug 9020