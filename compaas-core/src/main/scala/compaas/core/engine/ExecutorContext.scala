package compaas.core.engine

import org.graalvm.polyglot.*

import java.io.PrintStream

object ExecutorContext:
  def newBuilder() = new Builder()

  class Builder():
    private val contextBuilder = Context.newBuilder()

    def useEngine(engine: Engine): Builder =
      contextBuilder.engine(engine); this

    def allowExperimentalOptions(enable: Boolean): Builder =
      contextBuilder.allowExperimentalOptions(enable); this

    def esmEvalReturnsExports(enable: Boolean): Builder =
      contextBuilder.option("js.esm-eval-returns-exports", enable.toString()); this

    def ecmaScriptVersion(version: String): Builder =
      contextBuilder.option("js.ecmascript-version", version); this

    def strict(enable: Boolean): Builder =
      contextBuilder.option("js.strict", enable.toString()); this

    def intl402(enable: Boolean): Builder =
      contextBuilder.option("js.intl-402", enable.toString()); this

    def syntaxExtensions(enable: Boolean): Builder =
      contextBuilder.option("js.syntax-extensions", enable.toString()); this

    def load(enable: Boolean): Builder =
      contextBuilder.option("js.load", enable.toString()); this

    def console(enable: Boolean): Builder =
      contextBuilder.option("js.console", enable.toString()); this

    def print(enable: Boolean): Builder =
      contextBuilder.option("js.print", enable.toString()); this

    def polyglotBuiltin(enable: Boolean): Builder =
      contextBuilder.option("js.polyglot-builtin", enable.toString()); this

    def graalBuiltin(enable: Boolean): Builder =
      contextBuilder.option("js.graal-builtin", enable.toString()); this

    def out(out: PrintStream): Builder =
      contextBuilder.out(out); this

    def err(err: PrintStream): Builder =
      contextBuilder.err(err); this

    def foreignObjectPrototype(enable: Boolean): Builder =
      contextBuilder.option("js.foreign-object-prototype", enable.toString()); this

    def build(): ExecutorContext =
      given Context = contextBuilder.build()
      new ExecutorContext

case class Evaluated(value: Value)
case class Parsed(ast: Value):
  def execute(args: Any*): Evaluated = Evaluated(ast.execute(args*))
  def executeVoid(args: Any*): Unit  = ast.executeVoid(args*)

class ExecutorContext(using context: Context):
  def parse(source: Source): Parsed   = Parsed(context.parse(source))
  def eval(source: Source): Evaluated = Evaluated(context.eval(source))
