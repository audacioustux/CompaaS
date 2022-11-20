package compaas.http

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{ActorAttributes, Materializer, Supervision}
import akka.util.Timeout
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

import Helper.JsoniterScalaSupport.*
import Protocol.*
import concurrent.duration.DurationInt

object Routes:
  def apply(
      sentry: ActorRef[Sentry.Command]
  )(using ctx: ActorContext[?], mat: Materializer, ec: ExecutionContext): Route =
    val parallelism = Runtime.getRuntime.availableProcessors() * 2 - 1

    given JsonValueCodec[Out] = JsonCodecMaker.make
    given JsonValueCodec[In]  = JsonCodecMaker.make

    pathPrefix("@") {
      pathEndOrSingleSlash {
        import akka.actor.typed.scaladsl.AskPattern.Askable

        given Scheduler = ctx.system.scheduler
        given Timeout   = 2.seconds
        onComplete(
          sentry.ask[Flow[Either[Throwable, In], Out, ?]](Sentry.SendNewSessionFlow(_))
        ) {
          case Success(flow) =>
            handleWebSocketMessages(
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
                .map(in => Try(readFromString[In](in)).toEither) // parse to JSON
                .via(flow)
                .map(writeToString(_))
                .map[Message](TextMessage(_))
                .withAttributes(ActorAttributes.supervisionStrategy { e =>
                  ctx.log.error("something went wrong", e)
                  Supervision.Stop
                })
            )
          case Failure(e) =>
            ctx.log.error("failed to create session flow", e)
            complete(InternalServerError)
        }
      }
    }
