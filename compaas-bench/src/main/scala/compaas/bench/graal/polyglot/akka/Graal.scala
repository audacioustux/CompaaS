package compaas.bench.graal.polyglot.akka

import org.graalvm.polyglot.Engine

object Graal:
  lazy val engine = Engine.newBuilder().build()
