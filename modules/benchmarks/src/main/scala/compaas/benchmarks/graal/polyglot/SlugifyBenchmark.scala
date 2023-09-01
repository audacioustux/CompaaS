package compaas.benchmarks.graal.polyglot

import common.Graal
import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.{ Context, Engine, Source, Value }
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

object SlugifyBenchmark:
  final val opPerInvoke = 30

  val modules = Map(
    "js" ->
      Source
        .newBuilder("js", scala.io.Source.fromResource("js/slugify.mjs").mkString, "slugify.mjs")
        .mimeType("application/javascript+module")
        .build(),
    "wasm" ->
      Source
        .newBuilder(
          "wasm",
          ByteSequence
            .create(getClass.getClassLoader().getResourceAsStream("wasm/slugify.wasm").readAllBytes()),
          "slugify.wasm",
        )
        .build(),
  )

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1, jvmArgsAppend = Array("-Xmx16G"))
@Threads(Threads.MAX)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
class SlugifyBenchmark:
  import SlugifyBenchmark.*

  var engine: Engine = _

  @Param(Array("js", "wasm"))
  var module: String = _

  var source: Source = _

  var context: Context = _

  var executable: Value = _

  @Setup(Level.Trial)
  def setupEngine(): Unit = engine = Engine.create()

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
          context.eval(source).getMember("slugify")
        case "wasm" =>
          context.eval(source)
          context.getBindings("wasm").getMember("main").getMember("slugify")

  @TearDown(Level.Iteration)
  def closeContext(): Unit = context.close()

  @TearDown(Level.Trial)
  def closeEngine(): Unit = engine.close()

  private val n = Value.asValue(20)

  @Benchmark
  def invoke(): Value = executable.execute(n)

end SlugifyBenchmark
