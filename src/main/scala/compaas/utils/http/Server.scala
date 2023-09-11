package compaas.utils.http

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

object Server:
  case class ServerBindingFailure(ex: Throwable) extends CoordinatedShutdown.Reason

  // how long to wait for the server to stop gracefully
  val gracefulShutdownTimeout = 10.seconds

  def apply(interface: String, port: Int, route: Route)(using system: ActorSystem[?]) =
    given ec: ExecutionContext = system.executionContext

    Http()
      .newServerAt(interface, port)
      .bind(route)
      .map(_.addToCoordinatedShutdown(gracefulShutdownTimeout))
      .onComplete:
        case Success(binding) =>
          val address = binding.localAddress
          system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
        case Failure(ex) =>
          CoordinatedShutdown(system).run(ServerBindingFailure(ex))

