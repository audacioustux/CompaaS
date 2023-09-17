package compaas

import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server.Directives.*
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.typed.scaladsl.{ ActorSink, ActorSource }

import compaas.utils.http.{ Server, WebsocketRoute }

object ActorFlow:
  sealed trait In
  case object Complete              extends In
  case class Fail(ex: Throwable)    extends In
  case class HandleMessage(msg: In) extends In

  def apply[Out](ref: ActorRef[Any])(using Materializer): Flow[In, Out, ?] =
    val (recipient, source) = ActorSource
      .actorRef[Out](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        bufferSize = 1 << 6,
        // at-most-once delivery
        overflowStrategy = OverflowStrategy.dropHead,
      )
      .preMaterialize()

    val sink = ActorSink
      .actorRef(ref, onCompleteMessage = Complete, onFailureMessage = (exception) => Fail(exception))
      .contramap(HandleMessage(_))

    Flow.fromSinkAndSource(sink, source)

object IslandsFacade:

  def apply(config: Config)(using ctx: ActorContext[?]) =
    given ActorSystem[?]   = ctx.system
    given ExecutionContext = ctx.executionContext

    val interface = config.getString("interface")
    val port      = config.getInt("port")

    val route = pathEndOrSingleSlash:
      WebsocketRoute(Source.tick(1.second, 1.second, "Hello, World!"), Sink.foreach(println))

    Server(interface, port, route)
