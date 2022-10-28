import akka.actor.typed.SpawnProtocol.Spawn
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors.Receive
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop, Props, Signal, SpawnProtocol}
import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.*
import akka.stream.typed.scaladsl.ActorSink
import akka.util.{ByteString, Timeout}
import akka.{Done, NotUsed}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import scala.util.{Failure, Random, Success}

import concurrent.duration.DurationInt

object Server {
  sealed trait Message
  private final case class StartFailed(cause: Throwable)   extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop                                         extends Message

  def apply(host: String, port: Int): Behavior[Message] = Behaviors.setup { ctx =>
    given ActorSystem[?] = ctx.system

    val receptionistActor = ctx.spawnAnonymous(RootReceptionistActor());
    val routes = pathSingleSlash {
      handleWebSocketMessages(RootReceptionistFlow(receptionistActor))
    }

    val serverBinding: Future[Http.ServerBinding] =
      Http().newServerAt(host, port).bind(routes)

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
          binding.unbind()
          Behaviors.same
        }
    }

    def starting(wasStopped: Boolean): Behaviors.Receive[Message] = {
      Behaviors.receiveMessage[Message] {
        case StartFailed(cause) =>
          throw new RuntimeException("Server failed to start", cause)
        case Started(binding) =>
          ctx.log.info(
            "Server online at http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort
          )
          if wasStopped then ctx.self ! Stop
          running(binding)
        case Stop =>
          // got a stop message but haven't completed starting yet,
          // cannot stop until starting has completed
          starting(wasStopped = true)
      }
    }

    starting(wasStopped = false)
  }
}

@main def system: Future[Done] =
  val config    = ConfigFactory.load()
  val interface = config.getString("app.interface")
  val port      = config.getInt("app.port")

  given system: ActorSystem[Server.Message] =
    ActorSystem(Server(interface, port), "LessNoiseOrgServer")

  Await.ready(system.whenTerminated, Duration.Inf)
