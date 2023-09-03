package compaas

import org.slf4j.LoggerFactory

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost

object CompaaS:
  val logger = LoggerFactory.getLogger(getClass)

  def apply() = Behaviors.setup[Nothing] { context =>
    given system: ActorSystem[Nothing] = context.system

    // load compaas specific configuration
    val config = system.settings.config.getConfig("compaas")

    // start Akka Management
    AkkaManagement(system).start()
    // start bootstrap process for joining the cluster
    ClusterBootstrap(system).start()
    // start the pod deletion cost reporter
    PodDeletionCost(system).start()

    // init the http endpoint
    val interface = config.getString("http.interface")
    val port      = config.getInt("http.port")
    CompaaSServer(interface, port)

    Behaviors.empty
  }

end CompaaS
