package compaas.http

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import concurrent.duration.DurationInt

object Server:
  sealed trait Message

  sealed trait Event                                       extends Message
  private final case class StartFailed(cause: Throwable)   extends Event
  private final case class Started(binding: ServerBinding) extends Event

  sealed trait Command extends Message
  case object Stop     extends Command

  def apply(): Behavior[Message] =
    Behaviors.setup { ctx =>
      given system: ActorSystem[?] = ctx.system
      given ec: ExecutionContext   = ctx.executionContext
      given mat: Materializer      = Materializer(ctx)

      val sentry = ctx.spawn(Sentry(), "Sentry")

      val routes = Routes(sentry)(using ctx, mat, ec)

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

      def running(binding: ServerBinding) =
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

      def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
        Behaviors.receiveMessage {
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
