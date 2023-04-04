package compaas

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object Main:
  val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit =
    init()

  def init(): Unit =
    val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "compaas")

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
