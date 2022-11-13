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
  case object Ack                                                      extends Event
  case class IncomingMessage(message: In, ackTo: ActorRef[Ack.type])   extends Event
  case object Completed                                                extends Event
  case class Failed(cause: Throwable)                                  extends Event
  case class Init(ackTo: ActorRef[Ack.type], recipient: ActorRef[Out]) extends Event

  def handleIncomming(message: In)(using recipient: ActorRef[Out]): Unit =
    message match {
      case In.Ping =>
        recipient ! Out.Pong
    }

  private def withRecipient(recipient: ActorRef[Out]): Behavior[Event] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case IncomingMessage(message, ackTo) =>
          handleIncomming(message)(using recipient)
          ackTo ! Ack
          Behaviors.same
        case Completed =>
          Behaviors.stopped
        case Failed(cause) =>
          ctx.log.error("something went wrong", cause)
          Behaviors.stopped
        // TODO: handle backpressure
        case Ack => Behaviors.same
        case other =>
          ctx.log.warn("Unexpected message: {}", other)
          Behaviors.unhandled
      }
    }

  def apply() = Behaviors.setup[Event] { ctx =>
    Behaviors.withStash(1 << 3) { stash =>
      Behaviors.receiveMessage {
        case Init(ackTo, recipient) =>
          ackTo ! Ack
          stash.unstashAll(withRecipient(recipient))
        case other =>
          stash.stash(other)
          Behaviors.same
      }
    }
  }
}

object Receptionist {
  trait Event
  case class AskedForNewSession(who: ActorRef[Flow[Message, Message, ?]]) extends Event

  private def newSessionFlow()(using ctx: ActorContext[?]): Flow[Message, Message, ?] = {
    given Materializer = Materializer(ctx)

    val parallelism = Runtime.getRuntime.availableProcessors() * 2 - 1

    val sessionId = UUID.randomUUID()
    val session   = ctx.spawn(Session(), s"Session-${sessionId}")

    val (recipient: ActorRef[Out], source: Source[Out, NotUsed]) = {
      import Session.*

      ActorSource
        .actorRefWithBackpressure[Out, Ack.type](
          ackTo = session,
          ackMessage = Ack,
          completionMatcher = { case Out.Completed => CompletionStrategy.draining },
          failureMatcher = { case Out.Failed(exceptionId, message) =>
            RuntimeException(s"[$exceptionId] $message")
          }
        )
        .preMaterialize()
    }

    val sink: Sink[In, NotUsed] = {
      import Session.*

      ActorSink.actorRefWithBackpressure(
        ref = session,
        messageAdapter = (ackTo: ActorRef[Ack.type], msg) => IncomingMessage(msg, ackTo),
        onInitMessage = (ackTo: ActorRef[Ack.type]) => Init(ackTo, recipient),
        ackMessage = Ack,
        onCompleteMessage = Completed,
        onFailureMessage = (exception) => Failed(exception)
      )
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
      .mapAsync(parallelism)(_.toStrict(5.second)) // don't wait too long
      .map(_.text)
      .map(readFromString[In](_))
      .via(Flow.fromSinkAndSource(sink, source)) // sink and source not coupled
      .map(writeToString(_))
      .map[Message](TextMessage(_))
      .withAttributes(ActorAttributes.supervisionStrategy { e =>
        // TODO: log error
        Supervision.Stop
      })
  }

  def apply()(using ctx: ActorContext[?]): Behavior[Event] = Behaviors.setup { ctx =>
    given Materializer = Materializer(ctx)

    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AskedForNewSession(who) =>
          who ! newSessionFlow()(using ctx)
          Behaviors.same
      }
    }
  }
}

object Routes {
  def apply()(using ctx: ActorContext[?]): Route = {
    given ActorSystem[?] = ctx.system

    val gateway = ctx.spawn(Receptionist(), "Receptionist")

    pathPrefix("@") {
      pathEndOrSingleSlash {
        import akka.actor.typed.scaladsl.AskPattern.*

        given Timeout =
          Timeout(3.seconds) // drop request if it takes too long to create a session actor
        val flow = gateway.ask[Flow[Message, Message, ?]](Receptionist.AskedForNewSession(_))

        onComplete(flow) {
          // TODO: Do not expose error message, log it instead with an Id, and return to the client
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(flow) => handleWebSocketMessages(flow)
        }
      }
    }
  }
}
