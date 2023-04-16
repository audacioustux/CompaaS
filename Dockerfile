FROM ghcr.io/graalvm/graalvm-ce:22

RUN gu install native-image js wasm python

COPY . .

RUN mkdir -p /agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /agent/opentelemetry-javaagent.jar

EXPOSE 8080 8558 25520

ENV JAVA_OPTS "-XX:+EagerJVMCI -javaagent:/agent/opentelemetry-javaagent.jar -Dotel.traces.exporter=none "

ENTRYPOINT bin/compaas