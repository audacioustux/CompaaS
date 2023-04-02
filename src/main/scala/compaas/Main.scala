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
    (1 to 2).foreach { nr =>
      val config: Config = ConfigFactory
        .parseString(s"""|akka.remote.artery.canonical.hostname = "127.0.0.$nr"
                         |akka.management.http.hostname = "127.0.0.$nr"
                         |""".stripMargin)
        .withFallback(ConfigFactory.load())

      init(config)
    }

  def init(config: Config): Unit =
    val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "compaas")

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
