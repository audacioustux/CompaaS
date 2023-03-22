package compaas.core

import compaas.core.engine.*
import org.graalvm.polyglot.*

@main def Main() = {
  val src = """
    export default function add(a, b) {
      throw new Error("test");

      return a + b;
    }
  """

  val source =
    Source.newBuilder("js", src, "main.mjs").mimeType("application/javascript+module").build()

  val stdoutStream             = new java.io.ByteArrayOutputStream()
  val stderrStream             = new java.io.ByteArrayOutputStream()
  val uncaughtExceptionStream  = new java.io.ByteArrayOutputStream()
  val stdoutPrinter            = new java.io.PrintStream(stdoutStream)
  val stderrPrinter            = new java.io.PrintStream(stderrStream)
  val uncaughtExceptionPrinter = new java.io.PrintStream(uncaughtExceptionStream)

  val engine = Engine.newBuilder().build()
  val context = Context
    .newBuilder()
    .allowExperimentalOptions(true)
    .option("js.ecmascript-version", "2022")
    .option("js.esm-eval-returns-exports", "true")
    .option("js.strict", "true")
    .option("js.intl-402", "true")
    .option("js.syntax-extensions", "true")
    .option("js.load", "false")
    .option("js.console", "false")
    .option("js.print", "false")
    .option("js.polyglot-builtin", "false")
    .option("js.graal-builtin", "false")
    .option("js.foreign-object-prototype", "false")
    .option("js.strict", "true")
    .out(stdoutPrinter)
    .err(stderrPrinter)
    .engine(engine)
    .build()

  val module = context.eval(source)

  try {
    module.getMember("default").execute(1, 2)
  } catch {
    case e: PolyglotException =>
      uncaughtExceptionPrinter.println(e.getGuestObject().getMember("stack"))
  }

  println("stdout:")
  println(stdoutStream.toString())
  println("stderr:")
  println(stderrStream.toString())
  println("uncaughtException:")
  println(uncaughtExceptionStream.toString())
}
