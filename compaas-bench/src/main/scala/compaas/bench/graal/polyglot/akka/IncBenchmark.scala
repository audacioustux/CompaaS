package compaas.bench.graal.polyglot.akka

import akka.Done
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector, PostStop}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import compaas.bench.graal.polyglot.common.Graal
import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.{Context, Engine, Source, Value}
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.nio.file.{Files, Paths}
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.Await

import concurrent.duration.DurationInt

object IncBenchmark {
  val modules = Map(
    "js" -> Source
      .newBuilder("js", "export const inc = (n) => n + 1", "inc.js")
      .mimeType("application/javascript+module")
      .build(),
    "wasm" -> Source
      .newBuilder(
        "wasm",
        ByteSequence.create(
          getClass.getClassLoader().getResourceAsStream("wasm/inc.wasm").readAllBytes()
        ),
        "inc.wasm"
      )
      .build()
  )

  val n = Value.asValue(0)

  final val threads      = 1
  final val opPerNPA     = 10_000
  final val numberOfNPAs = threads
  final val opPerInvoke  = opPerNPA * numberOfNPAs
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1, jvmArgsAppend = Array("-Xmx16G"))
@Threads(Threads.MAX)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
class IncBenchmark {
  import IncBenchmark.*

  implicit var system: ActorSystem[IncBenchmarkActors.Start] = _

  given Timeout = Timeout(30.seconds)

  @Param(Array("js", "wasm"))
  var module: String = _

  @Setup(Level.Trial)
  def setup(using Blackhole): Unit = {
    system = ActorSystem(
      IncBenchmarkActors.Supervisor(numberOfNPAs, modules(module)),
      "inc",
      ConfigFactory.parseString(
        s"akka.actor.default-dispatcher.fork-join-executor.parallelism-max = $threads"
      )
    )
  }

  @Benchmark
  @OperationsPerInvocation(opPerInvoke)
  def inc(): Unit = {
    Await.result(system.ask(IncBenchmarkActors.Start(_)), 30.seconds)
  }

  @TearDown(Level.Trial)
  def shutdown(): Unit = {
    system.terminate()
    Await.ready(system.whenTerminated, 15.seconds)
  }
}

object IncBenchmarkActors {
  import IncBenchmark.*

  case class Start(replyTo: ActorRef[Done])
  case class Execute(times: Int, replyTo: ActorRef[Done])

  def spikingActorBehavior(threshold: Int, replyTo: ActorRef[Done]) = Behaviors.setup { _ =>
    var count = 0

    Behaviors.receiveMessage { _ =>
      count += 1
      if count == threshold then {
        replyTo ! Done
        count = 0
      }

      Behaviors.same
    }
  }

  def Supervisor(numberOfNPAs: Int, source: Source)(using blackhole: Blackhole) = Behaviors.setup {
    ctx =>
      val npa_s = (1 to numberOfNPAs).map { _ =>
        ctx.spawnAnonymous(IncActor(source))
      }

      Behaviors.receiveMessage { msg =>
        msg match {
          case Start(replyTo) =>
            val spikingActor = ctx.spawnAnonymous(spikingActorBehavior(numberOfNPAs, replyTo))
            npa_s.foreach { npa =>
              npa ! Execute(opPerNPA, spikingActor)
            }
            Behaviors.same
        }
      }
  }

  def IncActor(source: Source)(using blackhole: Blackhole) =
    Behaviors.setup { ctx =>
      val language = source.getLanguage()

      val context = {
        val builder = Context.newBuilder().engine(Graal.engine)
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

      val executable = language match {
        case "js" =>
          context.eval(source).getMember("inc")
        case "wasm" =>
          context.eval(source)
          context.getBindings("wasm").getMember("main").getMember("inc")
      }

      Behaviors
        .receiveMessage { msg =>
          msg match {
            case Execute(times, replyTo) =>
              blackhole.consume(executable.execute(n))

              if times > 1 then ctx.self ! Execute(times - 1, replyTo)
              else replyTo ! Done

              Behaviors.same
          }
        }
        .receiveSignal({ case (_, PostStop) =>
          context.close()
          Behaviors.same
        })
    }
}
