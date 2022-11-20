package compaas.http

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior, PostStop, Scheduler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import akka.util.Timeout

import java.util.UUID
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

import concurrent.duration.DurationInt

object Server:
  sealed trait Event
  private final case class StartFailed(cause: Throwable)   extends Event
  private final case class Started(binding: ServerBinding) extends Event
  case object Stop                                         extends Event

  def apply(): Behavior[Event] =
    Behaviors.setup { ctx =>
      given system: ActorSystem[?] = ctx.system
      given ExecutionContext       = ctx.executionContext
      given Scheduler              = system.scheduler

      val sentry = ctx.spawn(Sentry(), "Sentry")

      import akka.actor.typed.scaladsl.AskPattern.*
      given Timeout = 2.seconds
      val route     = sentry.ask[Route](Sentry.AskedForRoute(_))

      val config   = system.settings.config.getConfig("akka.http.server")
      val hostname = config.getString("hostname")
      val port     = config.getInt("port")

      route.onComplete {
        case Success(route) =>
          val serverBinding = Http()
            .newServerAt(hostname, port)
            .bind(route)
            .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

          ctx.pipeToSelf(serverBinding) {
            case Success(binding) => Started(binding)
            case Failure(ex)      => StartFailed(ex)
          }
        case Failure(cause) =>
          ctx.self ! StartFailed(cause)
      }

      def running(binding: ServerBinding): Behavior[Event] =
        Behaviors
          .receiveMessagePartial[Event] { case Stop =>
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

      def starting(wasStopped: Boolean): Behaviors.Receive[Event] =
        Behaviors.receiveMessage[Event] {
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

      starting(wasStopped = false)
    }
