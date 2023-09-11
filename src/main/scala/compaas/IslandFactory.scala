package compaas

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server.Directives.*
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import com.typesafe.config.Config

import compaas.utils.http.{ Server, WebsocketRoute }

object IslandFactory:

  def apply(config: Config)(using ctx: ActorContext[?]) =
    given ActorSystem[?]   = ctx.system
    given ExecutionContext = ctx.executionContext

    val interface = config.getString("interface")
    val port      = config.getInt("port")

    val route = pathEndOrSingleSlash:
      WebsocketRoute(Source.tick(1.second, 1.second, "Hello, World!"), Sink.foreach(println))

    Server(interface, port, route)
