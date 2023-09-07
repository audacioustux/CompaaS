package compaas

import akka.actor.typed.*
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.*
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost

object Main:

  def apply() = Behaviors.setup[Nothing] { context =>
    given system: ActorSystem[?] = context.system

    // load compaas specific configuration
    val config = system.settings.config.getConfig("compaas")

    // start Akka Management
    AkkaManagement(system).start()
    // start bootstrap process for joining the cluster
    ClusterBootstrap(system).start()
    // start the pod deletion cost reporter
    PodDeletionCost(system).start()

    // start the HTTP bridge
    HttpBridge(config.getConfig("http"))

    Behaviors.empty
  }

  def main(args: Array[String]): Unit =
    ActorSystem(Main(), "compaas")
    ()

end Main
