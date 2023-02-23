package compaas.core.engine

import org.graalvm.polyglot.*

object Executor {
  def newBuilder() = new Builder
  class Builder {
    private val engineBuilder = Engine.newBuilder()

    def build(): Executor = {
      given engine: Engine = engineBuilder.build()
      new Executor
    }
  }
}

class Executor(using engine: Engine) {
  def createContext(languageId: LanguageId): ExecutorContext =
    ExecutorContext.newBuilder(languageId).useEngine(engine).build()
}
