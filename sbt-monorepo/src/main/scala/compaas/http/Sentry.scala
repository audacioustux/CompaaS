package compaas.http

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{
  ActorAttributes,
  CompletionStrategy,
  Materializer,
  OverflowStrategy,
  Supervision
}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import concurrent.duration.DurationInt
import Helper.JsoniterScalaSupport.*
import Protocol.*

private object Receptionist {
  sealed trait Event
  // TODO: add traceId
  final case class IncomingMessage(message: Either[Throwable, In]) extends Event
  case object Completed                                      extends Event
  final case class Failed(cause: Throwable)                        extends Event

  def handle(msg: In)(using recipient: ActorRef[Out]) = msg match {
    case In.Echo(msg) => recipient ! Out.Echo(msg)
  }

  def apply(recipient: ActorRef[Out]) = Behaviors.setup[Event] { ctx =>
    Behaviors.receiveMessage {
      case IncomingMessage(Right(message)) =>
        handle(message)(using recipient)
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
        bufferSize = 1 << 6,
        // at-most-once delivery
        overflowStrategy = OverflowStrategy.dropHead
      )
      .preMaterialize()
}

// TODO: refactor to persistent actor
private object Session {
  private val parallelism = Runtime.getRuntime.availableProcessors() * 2 - 1

  private given JsonValueCodec[Out] = JsonCodecMaker.make
  private given JsonValueCodec[In]  = JsonCodecMaker.make

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

    Flow[Message]
      .filter {
        case _: TextMessage    => true
        case bm: BinaryMessage =>
          // Ignore binary messages, but drain data stream
          bm.dataStream.runWith(Sink.ignore)
          false
      }
      .collect { case tm: TextMessage => tm }
      // at-most-once, unordered delivery
      .mapAsyncUnordered(parallelism)(_.toStrict(1.second).map(_.text))
      .map(in => Try(readFromString[In](in)).toEither)  // parse to JSON
      .via(Flow.fromSinkAndSourceCoupled(sink, source)) // sink and source not coupled
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

  def apply(): Behavior[Event] = Behaviors.setup { ctx =>
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
