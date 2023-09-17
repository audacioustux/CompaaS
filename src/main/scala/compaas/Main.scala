package compaas

import com.typesafe.config.{ Config, ConfigFactory }

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.*
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost

object Main:

  def apply(config: Config) = Behaviors.setup[Nothing] { implicit ctx =>
    IslandsFacade(config.getConfig("islands-facade"))

    Behaviors.empty
  }

  def main(args: Array[String]): Unit =
    val config = ConfigFactory.load()

    val system = ActorSystem(Main(config.getConfig("compaas")), "compaas")

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
    PodDeletionCost(system).start()
