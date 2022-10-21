package com.audacioustux

import akka.Done
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.WSProbe
import akka.stream.scaladsl.*
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import scala.util.{Failure, Random, Success}

import concurrent.duration.DurationInt

def greeter(using system: ActorSystem[?]): Flow[Message, Message, Any] =
  Flow[Message].mapConcat {
    case tm: TextMessage =>
      TextMessage(Source.single("henlo ") ++ tm.textStream ++ Source.single("!!!")) :: Nil
    case bm: BinaryMessage =>
      // ignore binary messages but drain content to avoid the stream being clogged
      bm.dataStream.runWith(Sink.ignore)
      Nil
  }

class Routes(using system: ActorSystem[?]) {
  lazy val theJobRoutes: Route = pathSingleSlash {
    handleWebSocketMessages(greeter)
  }
}

object Server {
  sealed trait Message
  private final case class StartFailed(cause: Throwable)   extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop                                         extends Message

  def apply(host: String, port: Int): Behavior[Message] = Behaviors.setup { ctx =>
    given ActorSystem[?] = ctx.system

    val routes = new Routes()

    val serverBinding: Future[Http.ServerBinding] =
      Http().newServerAt(host, port).bind(routes.theJobRoutes)

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
