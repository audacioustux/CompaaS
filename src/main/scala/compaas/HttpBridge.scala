package compaas

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives.*
import akka.stream.scaladsl.Flow
import com.typesafe.config.Config

import compaas.utils.http.Server

object HttpBridge:

  def apply(config: Config)(using ActorSystem[?]) =
    val interface = config.getString("interface")
    val port      = config.getInt("port")

    def route = pathEndOrSingleSlash:
      handleWebSocketMessages(Flow[Message].map(_ => TextMessage("Hello World!")))

    Server(interface, port, route)
