package compaas.core.engine

import org.graalvm.polyglot.*

object ExecutorContext {
  def newBuilder(languageId: LanguageId) = new Builder(languageId)
  class Builder(languageId: LanguageId) {
    private val contextBuilder = Context.newBuilder(languageId.codename)

    def useEngine(engine: Engine): Builder =
      contextBuilder.engine(engine); this

    def esmEvalReturnsExports(enable: Boolean): Builder =
      contextBuilder
        .allowExperimentalOptions(true)
        .option("js.esm-eval-returns-exports", enable.toString()); this

    def ecmaScriptVersion(version: String): Builder =
      contextBuilder.option("js.ecmascript-version", version); this

    def strict(enable: Boolean): Builder =
      contextBuilder.option("js.strict", enable.toString()); this

    def foreignObjectPrototype(enable: Boolean): Builder =
      contextBuilder.option("js.foreign-object-prototype", enable.toString()); this

    def build(): ExecutorContext = {
      given Context = contextBuilder.build()
      new ExecutorContext
    }
  }
}

class ExecutorContext(using context: Context) {
  def initialize(source: Source): Value = context.eval(source)
}
