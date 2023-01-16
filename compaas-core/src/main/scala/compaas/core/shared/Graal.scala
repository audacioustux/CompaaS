package compaas.core.shared

import org.graalvm.polyglot.Engine

object Graal:
  lazy val engine = Engine.newBuilder().build()
