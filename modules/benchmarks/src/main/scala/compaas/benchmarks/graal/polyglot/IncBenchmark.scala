package compaas.benchmarks.graal.polyglot

import common.Graal
import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.{ Context, Source, Value }
import org.openjdk.jmh.annotations.*

import java.util.concurrent.TimeUnit

object IncBenchmark:

  val modules = Map(
    "js" ->
      Source
        .newBuilder("js", "export const inc = (n) => n + 1", "inc.js")
        .mimeType("application/javascript+module")
        .build(),
    "wasm" ->
      Source
        .newBuilder(
          "wasm",
          ByteSequence.create(getClass.getClassLoader().getResourceAsStream("wasm/inc.wasm").readAllBytes()),
          "inc.wasm",
        )
        .build(),
  )

end IncBenchmark

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1, jvmArgsAppend = Array("-Xmx16G"))
@Threads(Threads.MAX)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
class IncBenchmark:
  import IncBenchmark.*

  @Param(Array("js", "wasm"))
  var module: String = _

  var source: Source = _

  var context: Context = _

  var executable: Value = _

  @Setup(Level.Iteration)
  def setup(): Unit =
    source = modules(module)

    val language = source.getLanguage()

    context =
      val builder = Context.newBuilder().engine(Graal.engine)
      language match
        case "js" =>
          builder.allowExperimentalOptions(true).option("js.esm-eval-returns-exports", "true").build()
        case "wasm" =>
          builder.build()

    executable =
      language match
        case "js" =>
          context.eval(source).getMember("inc")
        case "wasm" =>
          context.eval(source)
          context.getBindings("wasm").getMember("main").getMember("inc")

  end setup

  @TearDown(Level.Iteration)
  def closeContext(): Unit = context.close()

  private val n: Value = Value.asValue(0)

  @Benchmark
  def invoke(): Value = executable.execute(n)

end IncBenchmark
