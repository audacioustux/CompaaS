package compaas

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*

object CompaaSServer:

  def apply(interface: String, port: Int)(using system: ActorSystem[?]) =
    given ec: ExecutionContext = system.executionContext

    val routes = get:
      complete("Welcome to CompaaS!")
    val bound = Http().newServerAt(interface, port).bind(routes).map(_.addToCoordinatedShutdown(10.seconds))

    bound.onComplete:
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()

end CompaaSServer
