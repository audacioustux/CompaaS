package compaas.core.engine

import cats.instances.boolean
import org.graalvm.polyglot.*

import scala.collection.mutable.HashMap

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

class ExecutorContext(using context: Context) {
  val exports = HashMap[String, Value]()

  def initialize(source: Source): Unit =
    val exports = context.eval(source)
    this.exports.put(source.getName(), exports)
}

@main def blabla(): Unit = {
  val context = ExecutorContext
    .newBuilder()
    .useEngine(Engine.create())
    .esmEvalReturnsExports(true)
    .foreignObjectPrototype(true)
    .build()

  context.initialize(
    Source
      .newBuilder(
        "js",
        """|export function log(event) {
           |console.log("hello: ", event);
           |};""".stripMargin,
        "hello.mjs"
      )
      .build()
  )

  context.initialize(
    Source
      .newBuilder(
        "js",
        """|export function log(event) {
           |console.log("bello: ", event);
           |};""".stripMargin,
        "bello.mjs"
      )
      .build()
  )

  context.exports("hello.mjs").getMember("log").execute("audacioustux")
  context.exports("bello.mjs").getMember("log").execute("audacioustux")

}
