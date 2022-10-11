import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn
import scala.util.Random
import akka.http.scaladsl.Http

import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.ByteString

object Twin {
  final case class Greet(whom: String)

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    Behaviors.same
  }
}

object Main {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Twin.apply(), "hello")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val numbers =
      Source.fromIterator(() => Iterator.continually(Random.nextInt()))

    val route =
      path("random") {
        get {
          system ! Twin.Greet("World")
          complete(
            HttpEntity(
              ContentTypes.`text/plain(UTF-8)`,
              // transform each number to a chunk of bytes
              numbers.map(n => ByteString(s"$n\n"))
            )
          )
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(
      s"Server online at http://localhost:8080/\nPress RETURN to stop..."
    )
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
