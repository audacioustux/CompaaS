package compaas

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, DeathPactException, SupervisorStrategy}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import concurrent.duration.DurationInt

object System:
  def apply() = Behaviors.setup[Nothing] { ctx =>
    val httpServer = ctx.spawn(
      Behaviors
        .supervise(http.Server())
        .onFailure(
          SupervisorStrategy.restart.withLimit(maxNrOfRetries = 10, withinTimeRange = 20.seconds)
        ),
      "HttpServer"
    )

    Behaviors.empty
  }

@main def Main: Unit =
  given system: ActorSystem[Nothing] = ActorSystem(System(), "CompaaS")

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  Await.ready(system.whenTerminated, Duration.Inf)
