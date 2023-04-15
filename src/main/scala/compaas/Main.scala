package compaas

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost
import com.typesafe.config.{Config, ConfigFactory}
import compaas.core.Compaas

object Main:
  def main(args: Array[String]): Unit =
    given ActorSystem[?] = ActorSystem(Behaviors.empty, "compaas")

    startClusterBootstrap
    startClusterSharding
    startCompaasCore

  private def startClusterBootstrap(using ActorSystem[?]): Unit =
    AkkaManagement(summon).start()
    ClusterBootstrap(summon).start()
    PodDeletionCost(summon).start()

  private def startClusterSharding(using ActorSystem[?]): Unit =
    val sharding = ClusterSharding(summon)

  private def startCompaasCore(using ActorSystem[?]): Unit =
    Compaas()
