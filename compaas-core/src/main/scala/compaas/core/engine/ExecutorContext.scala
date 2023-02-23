package compaas.core.engine

import org.graalvm.polyglot.*

object ExecutorContext {
  def newBuilder(languageId: LanguageId) = new Builder(languageId)
  class Builder(languageId: LanguageId) {
    private val contextBuilder = Context.newBuilder(languageId.codename)

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
