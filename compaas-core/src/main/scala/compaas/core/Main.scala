package compaas.core

import org.graalvm.polyglot.*

import engine.*

@main def hello: Unit =
  val executor = Executor
    .newBuilder()
    .allowExperimentalOptions(true)
    .build()
  given context: ExecutorContext = executor.newContextBuilder().build()
  val module = Module(Source.newBuilder("js", "console.log('Hello, World!')", "hello.mjs").build())
