package compaas

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.control.NonFatal
object Main:
  def main(args: Array[String]): Unit =
    val guardian: ActorSystem[Nothing] = ActorSystem(Guardian(), "compaas")

    AkkaManagement(guardian).start()
    ClusterBootstrap(guardian).start()
