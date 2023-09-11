package compaas

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.*
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost

object Main:

  def apply() = Behaviors.setup[Nothing] { implicit ctx =>
    val config = ctx.system.settings.config.getConfig("compaas")

    IslandFactory(config.getConfig("island-factory"))

    Behaviors.empty
  }

  end apply

  def main(args: Array[String]): Unit =
    val system = ActorSystem(Main(), "compaas")

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
    PodDeletionCost(system).start()

end Main
