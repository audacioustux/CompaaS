package http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import akka.util.Timeout

import scala.util.{Failure, Success}

import concurrent.duration.DurationInt

object Routes {
  def apply()(using ctx: ActorContext[?]): Route = {
    given ActorSystem[?] = ctx.system

    val sentry = ctx.spawn(Sentry(), "Receptionist")

    pathPrefix("@") {
      pathEndOrSingleSlash {
        import akka.actor.typed.scaladsl.AskPattern.*
        import Sentry.AskedForNewSession

        // drop request if it takes too long to create a session actor
        given Timeout = Timeout(3.seconds)
        val flow      = sentry.ask[Flow[Message, Message, ?]](AskedForNewSession(_))

        onComplete(flow) {
          case Failure(ex) =>
            val errMsg = "Failed to create a new session"
            ctx.log.error(errMsg, ex)
            complete(InternalServerError, errMsg)
          case Success(flow) => handleWebSocketMessages(flow)
        }
      }
    }
  }
}
