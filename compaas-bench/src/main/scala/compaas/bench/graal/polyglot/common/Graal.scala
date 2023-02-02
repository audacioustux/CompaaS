package compaas.bench.graal.polyglot.common

import org.graalvm.polyglot.Engine

object Graal:
  lazy val engine = Engine.newBuilder().build()
