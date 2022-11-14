package http

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{ActorAttributes, Materializer, OverflowStrategy, Supervision}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import Helper.JsoniterScalaSupport.*
import Protocol.*

private given JsonValueCodec[Out] = JsonCodecMaker.make
private given JsonValueCodec[In]  = JsonCodecMaker.make

private object Receptionist {
  sealed trait Event
  // TODO: add traceId
  case class IncomingMessage(message: Either[Throwable, In]) extends Event
  case object Completed                                      extends Event
  case class Failed(cause: Throwable)                        extends Event

  def apply(recipient: ActorRef[Out]) = Behaviors.setup[Event] { ctx =>
    Behaviors.receiveMessage {
      case IncomingMessage(Right(message)) =>
        message match {
          case In.Ping =>
            recipient ! Out.Pong
        }
        Behaviors.same
      case IncomingMessage(Left(e)) =>
        val errMsg = s"failed to parse message"
        ctx.log.error(errMsg, e)
        recipient ! Out.Error(errMsg)
        Behaviors.same
      case Completed =>
        Behaviors.stopped
      case Failed(cause) =>
        ctx.log.error("something went wrong", cause)
        // TODO: restart?
        Behaviors.stopped
    }
  }
}

private object Recipient {
  def apply()(using Materializer): (ActorRef[Out], Source[Out, NotUsed]) =
    ActorSource
      .actorRef[Out](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        bufferSize = 1 << 4,
        overflowStrategy = OverflowStrategy.dropHead // TODO: ensure exactly-once delivery
      )
      .preMaterialize()
}

private object Session {
  def apply()(using ctx: ActorContext[?]): Flow[Message, Message, ?] = {
    given Materializer     = Materializer(ctx)
    given ExecutionContext = ctx.executionContext

    val (recipient, source) = Recipient()

    val receptionist = ctx.spawnAnonymous(Receptionist(recipient))
    val sink: Sink[Either[Throwable, In], NotUsed] = {
      import Receptionist.*

      ActorSink
        .actorRef(
          ref = receptionist,
          onCompleteMessage = Completed,
          onFailureMessage = (exception) => Failed(exception)
        )
        .contramap(IncomingMessage(_))
    }

    val parallelism = Runtime.getRuntime.availableProcessors() * 2 - 1

    Flow[Message]
      .filter {
        case _: TextMessage    => true
        case bm: BinaryMessage =>
          // Ignore binary messages, but drain data stream
          bm.dataStream.runWith(Sink.ignore)
          false
      }
      .collect { case tm: TextMessage => tm }
      .mapAsync(parallelism)(_ match {
        case TextMessage.Strict(text) => Future.successful(text)
        case TextMessage.Streamed(textStream) =>
          textStream.runFold(new StringBuilder())(_.append(_)).map(_.toString)
      })
      .map(in => Try(readFromString[In](in)).toEither) // parse to JSON
      .via(Flow.fromSinkAndSource(sink, source))       // sink and source not coupled
      .map(writeToString(_))
      .map[Message](TextMessage(_))
      .withAttributes(ActorAttributes.supervisionStrategy { e =>
        ctx.log.error("something went wrong", e)
        Supervision.Stop
      })
  }
}

object Sentry {
  trait Event
  case class AskedForNewSession(who: ActorRef[Flow[Message, Message, ?]]) extends Event

  def apply()(using ctx: ActorContext[?]): Behavior[Event] = Behaviors.setup { ctx =>
    given Materializer = Materializer(ctx)

    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AskedForNewSession(who) =>
          who ! Session()(using ctx)
          Behaviors.same
      }
    }
  }
}
