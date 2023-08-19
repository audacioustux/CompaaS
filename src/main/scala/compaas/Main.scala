package compaas

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.rollingupdate.kubernetes.PodDeletionCost
// import compaas.core.Compaas

object Main:
  def main(args: Array[String]): Unit =
    given system: ActorSystem[?] = ActorSystem(Guardian(), "compaas")

    // bootstrap
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
    PodDeletionCost(system).start()

object Guardian:
  def apply(): Behavior[?] =
    Behaviors.setup { context =>
      // val compaas = context.spawn(Compaas(), "compaas")

      Behaviors.empty
    }
