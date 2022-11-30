# CompaaS

## Monorepo with sbt package manager for Scala3 Codebase

### Usage

run `bin/dev` or `sbt dev`

### Prerequisites

- [scala 3](https://www.scala-lang.org/download/)
- [concurrently](https://github.com/open-cli-tools/concurrently)
- [nodemon](https://github.com/remy/nodemon)
- [graalvm](https://graalvm.org)

#### Set environment variables

``` bash
export GRAALVM_HOME="/path/to/graalvm/Contents/Home"
export PATH="/path/to/graalvm/Contents/Home/bin:$PATH"
export JAVA_HOME="/path/to/graalvm/Contents/Home"
```

### Todos

- [ ] better logging
- [ ] tracing
- [ ] message delivery gurantee
- [ ] <https://fuchsia.dev/reference/cml>
