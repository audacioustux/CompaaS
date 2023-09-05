package compaas

import akka.actor.typed.*
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.*
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost

import compaas.utils.http.Server

object Main:

  def apply() = Behaviors.setup[Nothing] { context =>
    given system: ActorSystem[Nothing] = context.system
    given ClusterSharding              = ClusterSharding(system)

    // load compaas specific configuration
    val config = system.settings.config.getConfig("compaas")

    // start Akka Management
    AkkaManagement(system).start()
    // start bootstrap process for joining the cluster
    ClusterBootstrap(system).start()
    // start the pod deletion cost reporter
    PodDeletionCost(system).start()

    // init the service
    val service = Service()

    // init the http endpoint
    val interface = config.getString("http.interface")
    val port      = config.getInt("http.port")
    Server(interface, port, service.route)

    Behaviors.empty
  }

  def main(args: Array[String]): Unit = ActorSystem(Main(), "compaas")

end Main
