import akka.actor.typed.SpawnProtocol.Spawn
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors.Receive
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop, Props, Signal, SpawnProtocol}
import akka.actor.{Actor, ActorLogging}
import akka.discovery.{Discovery, Lookup}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.scaladsl.*
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.util.{ByteString, Timeout}
import akka.{Done, NotUsed}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Failure, Random, Success}

import concurrent.duration.DurationInt

object Server {
  sealed trait Message
  private final case class StartFailed(cause: Throwable)   extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop                                         extends Message

  def apply(): Behavior[Message] =
    Behaviors.setup { ctx =>
      given system: ActorSystem[?] = ctx.system
      given ExecutionContext       = system.executionContext

      val receptionist = ctx.spawnAnonymous(ServiceReceptionist())

      val routes = Routes(ctx, receptionist)

      val config   = system.settings.config.getConfig("akka.http.server")
      val hostname = config.getString("hostname")
      val port     = config.getInt("port")
      val serverBinding = Http()
        .newServerAt(hostname, port)
        .bind(routes)
        .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

      ctx.pipeToSelf(serverBinding) {
        case Success(binding) => Started(binding)
        case Failure(ex)      => StartFailed(ex)
      }

      def running(binding: ServerBinding): Behavior[Message] = {
        Behaviors
          .receiveMessagePartial[Message] { case Stop =>
            ctx.log.info(
              "Stopping server http://{}:{}/",
              binding.localAddress.getHostString,
              binding.localAddress.getPort
            )
            Behaviors.stopped
          }
          .receiveSignal { case (_, PostStop) =>
            binding.unbind() // stop accepting new connections
            Behaviors.same
          }
      }

      def starting(wasStopped: Boolean): Behaviors.Receive[Message] = {
        Behaviors.receiveMessage[Message] {
          case StartFailed(cause) => throw new RuntimeException("Server failed to start", cause)
          case Started(binding) =>
            ctx.log.info(
              "Server online at http://{}:{}/",
              binding.localAddress.getHostString,
              binding.localAddress.getPort
            )
            if wasStopped then ctx.self ! Stop
            running(binding)
          // got a stop message but haven't completed starting yet,
          // cannot stop until starting has completed
          case Stop => starting(wasStopped = true)
        }
      }

      starting(wasStopped = false)
    }
}

object System {
  def apply() = Behaviors.setup[Nothing] { ctx =>
    ctx.spawn(Server(), "Server")
    Behaviors.empty
  }
}

@main def Main: Unit =
  given system: ActorSystem[Nothing] = ActorSystem(System(), "CompaaS")

  AkkaManagement(system).start()
  // ClusterBootstrap(system).start()

  Await.ready(system.whenTerminated, Duration.Inf)
