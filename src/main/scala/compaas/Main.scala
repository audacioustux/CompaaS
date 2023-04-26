package compaas

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost
import compaas.core.Compaas

object Main:
  def main(args: Array[String]): Unit =
    given system: ActorSystem[?] = ActorSystem(Behaviors.empty, "compaas")

    // bootstrap
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
    PodDeletionCost(system).start()
