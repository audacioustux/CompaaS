package compaas.core.engine

import org.graalvm.polyglot.*

class PolyglotEngine(val engine: Engine) extends AutoCloseable:
  override def close(): Unit = engine.close
