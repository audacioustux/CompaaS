package compaas.bench.graal.polyglot.akka

import akka.Done
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, PostStop}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import compaas.bench.graal.polyglot.common.Graal
import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.{Context, Engine, Source, Value}
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

import concurrent.duration.DurationInt

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
          getClass.getClassLoader().getResourceAsStream("wasm/nth_prime.wasm").readAllBytes()
        ),
        "nth-prime.wasm"
      )
      .build()
  )

  val n = Value.asValue(10_000)

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
class NthPrimeBenchmark {
  import NthPrimeBenchmark.*

  implicit var system: ActorSystem[NthPrimeBenchmarkActors.Start] = _

  given Timeout = Timeout(30.seconds)

  @Param(Array("js", "wasm"))
  var module: String = _

  @Setup(Level.Trial)
  def setup(using Blackhole): Unit = {
    system = ActorSystem(
      NthPrimeBenchmarkActors.Supervisor(numberOfNPAs, modules(module)),
      "nth-prime",
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
  }

  @Benchmark
  @OperationsPerInvocation(opPerInvoke)
  def nthPrime(): Unit = {
    Await.result(system.ask(NthPrimeBenchmarkActors.Start(_)), 30.seconds)
  }

  @TearDown(Level.Trial)
  def shutdown(): Unit = {
    system.terminate()
    Await.ready(system.whenTerminated, 15.seconds)
  }
}

object NthPrimeBenchmarkActors {
  import NthPrimeBenchmark.*

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
        ctx.spawnAnonymous(NthPrimeActor(source))
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

  def NthPrimeActor(source: Source)(using blackhole: Blackhole) =
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
          context.eval(source).getMember("nth_prime")
        case "wasm" =>
          context.eval(source)
          context.getBindings("wasm").getMember("main").getMember("nth_prime")
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
