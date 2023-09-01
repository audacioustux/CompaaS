package compaas.benchmarks.graal.polyglot.akka
import compaas.benchmarks.graal.polyglot.common.Graal
import akka.Done
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, PostStop}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.{Context, Source, Value}
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

import concurrent.duration.DurationInt

object SlugifyBenchmark:
  val modules = Map(
    "js" -> Source
      .newBuilder(
        "js",
        scala.io.Source
          .fromResource("js/slugify.mjs")
          .mkString,
        "slugify.mjs"
      )
      .mimeType("application/javascript+module")
      .build(),
    "wasm" -> Source
      .newBuilder(
        "wasm",
        ByteSequence.create(
          getClass.getClassLoader().getResourceAsStream("wasm/slugify.wasm").readAllBytes()
        ),
        "slugify.wasm"
      )
      .build()
  )

  val n = Value.asValue(20)

  final val threads      = 1
  final val opPerNPA     = 1_000_000
  final val numberOfNPAs = threads
  final val opPerInvoke  = opPerNPA * numberOfNPAs

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1, jvmArgsAppend = Array("-Xmx16G"))
@Threads(Threads.MAX)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
class SlugifyBenchmark:
  import SlugifyBenchmark.*

  implicit var system: ActorSystem[SlugifyBenchmarkActors.Start] = _

  given Timeout = Timeout(30.seconds)

  @Param(Array("js", "wasm"))
  var module: String = _

  // @Setup(Level.Trial)
  // def requireRightNumberOfThreads: Unit =
  //   require(threads == Runtime.getRuntime().availableProcessors())

  @Setup(Level.Trial)
  def setup(using Blackhole): Unit =
    system = ActorSystem(
      SlugifyBenchmarkActors.Supervisor(numberOfNPAs, modules(module)),
      "slugify",
      ConfigFactory.parseString(
        s"""
           |akka.actor.default-dispatcher {
           |  type = PinnedDispatcher
           |  executor = "thread-pool-executor"
           |  thread-pool-executor {
           |    fixed-pool-size = $threads
           |  }
           |}
           |""".stripMargin
      )
    )

  @Benchmark
  @OperationsPerInvocation(opPerInvoke)
  def slugify(): Unit =
    Await.result(system.ask(SlugifyBenchmarkActors.Start(_)), 30.seconds)

  @TearDown(Level.Trial)
  def shutdown(): Unit =
    system.terminate()
    Await.ready(system.whenTerminated, 15.seconds)

object SlugifyBenchmarkActors:
  import SlugifyBenchmark.*

  case class Start(replyTo: ActorRef[Done])
  case class Execute(times: Int, replyTo: ActorRef[Done])

  def spikingActorBehavior(threshold: Int, replyTo: ActorRef[Done]) = Behaviors.setup { _ =>
    var count = 0

    Behaviors.receiveMessage { _ =>
      count += 1
      if count == threshold then
        replyTo ! Done
        count = 0

      Behaviors.same
    }
  }

  def Supervisor(numberOfNPAs: Int, source: Source)(using blackhole: Blackhole) = Behaviors.setup { ctx =>
    val npa_s = (1 to numberOfNPAs).map { _ =>
      ctx.spawnAnonymous(SlugifyActor(source))
    }

    Behaviors.receiveMessage { msg =>
      msg match
        case Start(replyTo) =>
          val spikingActor = ctx.spawnAnonymous(spikingActorBehavior(numberOfNPAs, replyTo))
          npa_s.foreach { npa =>
            npa ! Execute(opPerNPA, spikingActor)
          }
          Behaviors.same
    }
  }

  def SlugifyActor(source: Source)(using blackhole: Blackhole) =
    Behaviors.setup { ctx =>
      val language = source.getLanguage()

      val context =
        val builder = Context.newBuilder().engine(Graal.engine)
        language match
          case "js" =>
            builder
              .allowExperimentalOptions(true)
              .option("js.esm-eval-returns-exports", "true")
              .build()
          case "wasm" =>
            builder.build()

      val executable = language match
        case "js" =>
          context.eval(source).getMember("slugify")
        case "wasm" =>
          context.eval(source)
          context.getBindings("wasm").getMember("main").getMember("slugify")

      Behaviors
        .receiveMessage { msg =>
          msg match
            case Execute(times, replyTo) =>
              blackhole.consume(executable.execute(n))

              if times > 1 then ctx.self ! Execute(times - 1, replyTo)
              else replyTo ! Done

              Behaviors.same
        }
        .receiveSignal({ case (_, PostStop) =>
          context.close()
          Behaviors.same
        })
    }
