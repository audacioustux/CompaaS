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
    given ActorSystem[?] = ActorSystem(Guardian(), "compaas")

    startClusterBootstrap
    startClusterSharding

  private def startClusterBootstrap(using ActorSystem[?]): Unit =
    AkkaManagement(summon).start()
    ClusterBootstrap(summon).start()
    PodDeletionCost(summon).start()

  private def startClusterSharding(using ActorSystem[?]): Unit =
    val sharding = ClusterSharding(summon)

object Guardian:
  def startCompaas(using ActorContext[?]): Unit =
    summon.spawn(Compaas(), "compaas-core")

  def apply() = Behaviors.setup { implicit ctx =>
    startCompaas

    Behaviors.unhandled
  }
