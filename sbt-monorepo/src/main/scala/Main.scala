import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object System {
  def apply() = Behaviors.setup[Nothing] { ctx =>
    ctx.spawn(http.Server(), "HttpServer")
    Behaviors.empty
  }
}

@main def Main: Unit =
  given system: ActorSystem[Nothing] = ActorSystem(System(), "CompaaS")

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  Await.ready(system.whenTerminated, Duration.Inf)
