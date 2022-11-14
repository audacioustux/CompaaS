package http

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.ws.TextMessage.{Streamed, Strict}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.model.{HttpResponse, ws}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.*
import akka.stream.scaladsl.*
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import akka.util.Timeout
import cats.syntax.either.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import common.types.ParsingError

import java.util.UUID
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import Helper.JsoniterScalaSupport.*
import Protocol.*

given JsonValueCodec[Out] = JsonCodecMaker.make
given JsonValueCodec[In]  = JsonCodecMaker.make

object Session {
  sealed trait Event
  case class IncomingMessage(message: Either[ParsingError, In]) extends Event
  case object Completed                                         extends Event
  case class Failed(cause: Throwable)                           extends Event

  def handleIncomming(message: In)(using recipient: ActorRef[Out]): Unit =
    message match {
      case In.Ping =>
        recipient ! Out.Pong
    }

  def apply(recipient: ActorRef[Out]) = Behaviors.setup[Event] { ctx =>
    Behaviors.receiveMessage {
      case IncomingMessage(Right(message)) =>
        handleIncomming(message)(using recipient)
        Behaviors.same
      case IncomingMessage(Left(e)) =>
        // TODO: should not expose the error details to the client
        recipient ! Out.Error(e.toString)
        Behaviors.same
      case Completed =>
        Behaviors.stopped
      case Failed(cause) =>
        ctx.log.error("something went wrong", cause)
        Behaviors.stopped
    }
  }
}

object SessionFlow {
  import Session.*

  def apply()(using ctx: ActorContext[?]): Flow[Message, Message, ?] = {
    given Materializer = Materializer(ctx)

    val parallelism = Runtime.getRuntime.availableProcessors() * 2 - 1

    val (recipient, source) = ActorSource
      .actorRef[Out](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        bufferSize = 1 << 4,
        overflowStrategy = OverflowStrategy.dropHead // TODO: ensure exactly-once delivery
      )
      .preMaterialize()

    val sessionId = UUID.randomUUID()
    val session   = ctx.spawn(Session(recipient), s"Session-${sessionId}")

    val sink: Sink[Either[ParsingError, In], NotUsed] = {
      ActorSink
        .actorRef(
          ref = session,
          onCompleteMessage = Completed,
          onFailureMessage = (exception) => Failed(exception)
        )
        .contramap(IncomingMessage(_))
    }

    Flow[Message]
      .filter {
        case _: TextMessage    => true
        case bm: BinaryMessage =>
          // Ignore binary messages, but drain data stream
          bm.dataStream.runWith(Sink.ignore)
          false
      }
      .collect { case tm: TextMessage => tm }
      // TODO: don't crash
      .mapAsync(parallelism)(_.toStrict(5.second)) // don't wait too long
      .map(_.text)
      .map(in => Try(readFromString[In](in)).toEither.leftMap(ParsingError.apply)) // parse to JSON
      .via(Flow.fromSinkAndSource(sink, source)) // sink and source not coupled
      .map(writeToString(_))
      .map[Message](TextMessage(_))
      .withAttributes(ActorAttributes.supervisionStrategy { e =>
        // TODO: log error
        Supervision.Stop
      })
  }
}

object Receptionist {
  trait Event
  case class AskedForNewSession(who: ActorRef[Flow[Message, Message, ?]]) extends Event

  def apply()(using ctx: ActorContext[?]): Behavior[Event] = Behaviors.setup { ctx =>
    given Materializer = Materializer(ctx)

    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AskedForNewSession(who) =>
          who ! SessionFlow()(using ctx)
          Behaviors.same
      }
    }
  }
}

object Routes {
  def apply()(using ctx: ActorContext[?]): Route = {
    given ActorSystem[?] = ctx.system

    val receptionist = ctx.spawn(Receptionist(), "Receptionist")

    pathPrefix("@") {
      pathEndOrSingleSlash {
        import akka.actor.typed.scaladsl.AskPattern.*
        import Receptionist.AskedForNewSession

        // drop request if it takes too long to create a session actor
        given Timeout = Timeout(3.seconds)
        val flow      = receptionist.ask[Flow[Message, Message, ?]](AskedForNewSession(_))

        onComplete(flow) {
          // TODO: Do not expose error message, log it instead with an Id, and return to the client
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(flow) => handleWebSocketMessages(flow)
        }
      }
    }
  }
}
