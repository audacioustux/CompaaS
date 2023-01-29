package compaas.bench.graal.polyglot

import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.{Context, Engine, Source, Value}
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

object SingleModuleInvokeBenchmark {
  final val opPerInvoke = 1_000

  val modules = Map(
    "noop-js" -> Source
      .newBuilder("js", "export const foo = (n) => n + 1", "noop.js")
      .mimeType("application/javascript+module")
      .build(),
    "slugify-js" -> Source
      .newBuilder(
        "js",
        Files.readString(
          Paths.get(
            "../examples/sources/js/slugify.mjs"
          )
        ),
        "slugify.mjs"
      )
      .mimeType("application/javascript+module")
      .build(),
    "nth-prime-wasm" -> Source
      .newBuilder(
        "wasm",
        ByteSequence.create(
          Files.readAllBytes(
            Paths.get(
              "../examples/sources/wasm/target/wasm32-unknown-unknown/release/nth_prime.wasm"
            )
          )
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
class SingleModuleInvokeBenchmark {
  import SingleModuleInvokeBenchmark.*

  var engine: Engine = _

  // @Param(Array("noop-js", "slugify-js", "noop-wasm", "slugify-wasm", "nth-prime-wasm"))
  @Param(Array("nth-prime-wasm"))
  var module: String = _
  var source: Source = _

  var context: Context = _

  var executable: () => Value = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    engine = Engine.create()
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
        case _ =>
          throw new IllegalArgumentException(s"Unsupported language: ${source.getLanguage()}")
      }
    }

    executable = language match {
      case "js" =>
        val foo = context.eval(source).getMember("foo")
        () => foo.execute(10_000)
      case "wasm" =>
        context.eval(source)
        val foo = context.getBindings("wasm").getMember("main").getMember("foo")
        () => foo.execute(10_000)
    }
  }

  @TearDown(Level.Iteration)
  def closeContext(): Unit = {
    context.close()
    engine.close()
  }

  @Benchmark
  def invoke(blackhole: Blackhole): Unit = {
    val i = executable()
    blackhole.consume(i)
  }
}
