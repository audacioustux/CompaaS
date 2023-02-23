package compaas.core.engine

import org.graalvm.polyglot.*

object ExecutorContext {
  def newBuilder() = new Builder
  class Builder {
    private val contextBuilder = Context.newBuilder()

    def useEngine(engine: Engine): Builder =
      contextBuilder.engine(engine); this

    def esmEvalReturnsExports(enable: Boolean): Builder =
      contextBuilder.allowExperimentalOptions(true).option("js.esm-eval-returns-exports", "true")
      this

    def foreignObjectPrototype(enable: Boolean): Builder =
      contextBuilder.option("js.foreign-object-prototype", "true"); this

    def build(): ExecutorContext = {
      given Context = contextBuilder.build()
      new ExecutorContext
    }
  }
}

class ExecutorContext(using context: Context) {}
