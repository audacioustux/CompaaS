package http

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.event.ActorClassifier
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
  trait Event
  case class Init(ackTo: ActorRef[Ack])                         extends Event
  case class IncomingMessage(message: In, ackTo: ActorRef[Ack]) extends Event
  case object Completed                                         extends Event
  case class Failed(cause: Throwable)                           extends Event

  def apply(customer: ActorRef[Out]) = Behaviors.setup[Event] { ctx =>
    Behaviors.receiveMessage {
      case IncomingMessage(message, ackTo) =>
        message match {
          case In.Request(data) =>
            customer ! Out.Response(data)
            ackTo ! Ack
        }
        ackTo ! Ack
        Behaviors.same
      case Init(ackTo) =>
        ackTo ! Ack
        Behaviors.same
      case Completed =>
        Behaviors.stopped
      case Failed(cause) =>
        throw new RuntimeException("Session failed", cause)
    }
  }
}

object PushSource {
  sealed trait Event
  case object Completed            extends Event
  case class Failed(ex: Throwable) extends Event

  def apply()(using
      ctx: ActorContext[?],
      mat: Materializer
  ): (ActorRef[Out], Source[Out, NotUsed]) =
    val ackTo = ctx.spawnAnonymous(
      // TODO: handle backpressure properly
      Behaviors.receiveMessage { case Ack => Behaviors.same }
    )

    ActorSource
      .actorRefWithBackpressure(
        ackTo,
        ackMessage = Ack,
        completionMatcher = { case Completed => CompletionStrategy.draining },
        failureMatcher = { case Failed(ex) => ex }
      )
      .collect { case out: Out => out }
      // TODO: refactor
      .toMat(BroadcastHub.sink[Out](1 << 8))(Keep.both)
      .run()
}

object Receptionist {
  trait Event
  case class AskedForNewSession(who: ActorRef[Flow[Message, Message, ?]]) extends Event

  private def newSessionFlow()(using
      ctx: ActorContext[?],
      mat: Materializer
  ): Flow[Message, Message, ?] = {
    val parallelism = Runtime.getRuntime.availableProcessors() * 2 - 1

    val (customer, source) = PushSource()
    val session            = ctx.spawn(Session(customer), s"Session-${UUID.randomUUID()}")

    val sink: Sink[In, NotUsed] = {
      import Session.*

      ActorSink.actorRefWithBackpressure(
        ref = session,
        messageAdapter = (ackTo: ActorRef[Ack], msg) => IncomingMessage(msg, ackTo),
        onInitMessage = (ackTo: ActorRef[Ack]) => Init(ackTo),
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
      .mapAsync(parallelism)(
        _.toStrict(5.second)
      ) // stream better crash if message takes too long to receive
      .map(_.text)
      .map(readFromString[In](_))
      .via(
        Flow.fromSinkAndSource(sink, source)
      )
      .map(writeToString(_))
      .map[Message](TextMessage(_))
      .withAttributes(ActorAttributes.supervisionStrategy { e =>
        // TODO: log error
        Supervision.Stop
      })
  }

  def apply()(using Materializer): Behavior[Event] = Behaviors.setup { ctx =>
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
  def apply()(using
      ctx: ActorContext[?]
  ): Route = {
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
