package compaas.core.engine

import cats.instances.boolean
import org.graalvm.polyglot.*

object Executor {
  def newBuilder() = new Builder
  class Builder {
    private val engineBuilder = Engine.newBuilder()

    def build(): Executor = {
      val engine                = engineBuilder.build()
      given ec: ExecutorContext = ExecutorContext.newBuilder().useEngine(engine).build()

      new Executor
    }
  }
}

class Executor(using ec: ExecutorContext) {}
