package http

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.event.ActorClassifier
import akka.http.scaladsl.model.StatusCodes.*
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

object Receptionist {
  trait Event
  case class AskedForNewSession(who: ActorRef[Flow[Message, Message, ?]]) extends Event

  def apply()(using Materializer): Behavior[Event] = Behaviors.setup { ctx =>
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AskedForNewSession(who) =>
          who ! Flow[Message].mapConcat {
            case tm: TextMessage =>
              TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
            case bm: BinaryMessage =>
              // ignore binary messages but drain content to avoid the stream being clogged
              bm.dataStream.runWith(Sink.ignore)
              Nil
          }
          Behaviors.same
      }
    }
  }
}

object Routes {
  def apply()(using
      ctx: ActorContext[?]
  ): Route = {
    import akka.actor.typed.scaladsl.AskPattern.*

    given ActorSystem[?] = ctx.system

    val gateway = ctx.spawn(Receptionist(), "Receptionist")

    pathPrefix("@") {
      pathEndOrSingleSlash {
        given Timeout = Timeout(3.seconds)
        val flow      = gateway.ask[Flow[Message, Message, ?]](Receptionist.AskedForNewSession(_))

        onComplete(flow) {
          // TODO: Do not expose error message, log it instead with an Id, and return to the client
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(flow) => handleWebSocketMessages(flow)
        }
      }
    }
  }
}
