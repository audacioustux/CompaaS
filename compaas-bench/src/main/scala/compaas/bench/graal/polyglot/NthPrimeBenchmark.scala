package compaas.bench.graal.polyglot

import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.{Context, Engine, Source, Value}
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

object NthPrimeBenchmark {
  val modules = Map(
    "js" -> Source
      .newBuilder(
        "js",
        scala.io.Source
          .fromResource("js/nth-prime.mjs")
          .mkString,
        "nth-prime.mjs"
      )
      .mimeType("application/javascript+module")
      .build(),
    "wasm" -> Source
      .newBuilder(
        "wasm",
        ByteSequence.create(
          scala.io.Source
            .fromResource("wasm/nth-prime.wasm")
            .map(_.toByte)
            .toArray
        ),
        "nth-prime.wasm"
      )
      .build()
  )
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1, jvmArgsAppend = Array("-Xmx16G"))
@Threads(Threads.MAX)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
class NthPrimeBenchmark {
  import NthPrimeBenchmark.*

  var engine: Engine = _

  @Param(Array("js", "wasm"))
  var module: String = _
  var source: Source = _

  var context: Context = _

  var executable: Value = _

  @Setup(Level.Trial)
  def setupEngine(): Unit = {
    engine = Engine.create()
  }

  @Setup(Level.Iteration)
  def setup(): Unit = {
    source = modules(module)

    val language = source.getLanguage()

    context = {
      val builder = Context.newBuilder().engine(engine)
      language match {
        case "js" =>
          builder
            .allowExperimentalOptions(true)
            .option("js.esm-eval-returns-exports", "true")
            .build()
        case "wasm" =>
          builder.build()
      }
    }

    executable = language match {
      case "js" =>
        context.eval(source).getMember("nth_prime")
      case "wasm" =>
        context.eval(source)
        context.getBindings("wasm").getMember("main").getMember("nth_prime")
    }
  }

  @TearDown(Level.Iteration)
  def closeContext(): Unit = {
    context.close()
  }

  @TearDown(Level.Trial)
  def closeEngine(): Unit = {
    engine.close()
  }

  private val n = Value.asValue(10_000)

  @Benchmark
  def invoke(): Value = {
    executable.execute(n)
  }
}
