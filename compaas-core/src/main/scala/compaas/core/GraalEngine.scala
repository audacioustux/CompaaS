package compaas.core

import org.graalvm.polyglot.*

import java.io.PrintStream

object GraalEngine:
  def apply(permittedLanguages: String*)      = newBuilder(permittedLanguages*).build()
  def newBuilder(permittedLanguages: String*) = Builder(Engine.newBuilder(permittedLanguages*))

  class Builder(private val builder: Engine#Builder):
    def allowExperimentalOptions(value: Boolean) = Builder(builder.allowExperimentalOptions(value))
    def useIsolate(value: Boolean)               = Builder(builder.option("engine.SpawnIsolate", value.toString()))
    def build()                                  = new GraalEngine(builder.build())

class GraalEngine(private val engine: Engine):
  object GraalContext:
    def apply(permittedLanguages: String*)      = newBuilder(permittedLanguages*).build()
    def newBuilder(permittedLanguages: String*) = Builder(Context.newBuilder(permittedLanguages*).engine(engine))

    class Builder(private val builder: Context#Builder):
      def allowExperimentalOptions(value: Boolean) = Builder(builder.allowExperimentalOptions(value))
      def esmEvalReturnsExports(enable: Boolean) = Builder(
        builder.option("js.esm-eval-returns-exports", enable.toString())
      )
      def ecmaScriptVersion(version: String) = Builder(builder.option("js.ecmascript-version", version))
      def strict(enable: Boolean)            = Builder(builder.option("js.strict", enable.toString()))
      def intl402(enable: Boolean)           = Builder(builder.option("js.intl-402", enable.toString()))
      def syntaxExtensions(enable: Boolean)  = Builder(builder.option("js.syntax-extensions", enable.toString()))
      def load(enable: Boolean)              = Builder(builder.option("js.load", enable.toString()))
      def console(enable: Boolean)           = Builder(builder.option("js.console", enable.toString()))
      def print(enable: Boolean)             = Builder(builder.option("js.print", enable.toString()))
      def polyglotBuiltin(enable: Boolean)   = Builder(builder.option("js.polyglot-builtin", enable.toString()))
      def graalBuiltin(enable: Boolean)      = Builder(builder.option("js.graal-builtin", enable.toString()))
      def foreignObjectPrototype(enable: Boolean) = Builder(
        builder.option("js.foreign-object-prototype", enable.toString())
      )
      def out(out: PrintStream) = Builder(builder.out(out))
      def err(err: PrintStream) = Builder(builder.err(err))

      def build() = new GraalContext(builder.build())

  class GraalContext(private val ctx: Context):
    def parse(source: Source) = ctx.parse(source)
