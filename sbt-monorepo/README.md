# CompaaS

## Monorepo with sbt package manager for Scala3 Codebase

### Usage

run `bin/dev` or `sbt dev`

### Prerequisites

- [Scala 3](https://www.scala-lang.org/download/)

#### Install GraalVM

``` bash
bash <(curl -sL <https://get.graalvm.org/ee-token>)
bash <(curl -sL <https://get.graalvm.org/jdk>)
```

#### Set environment variables

``` bash
export GRAALVM_HOME="/path/to/graalvm-ee-java17-22.3.0/Contents/Home"
export PATH="/path/to/graalvm-ee-java17-22.3.0/Contents/Home/bin:$PATH"
export JAVA_HOME="/path/to/graalvm-ee-java17-22.3.0/Contents/Home"
```

### Todos

- [ ] better logging
- [ ] tracing
