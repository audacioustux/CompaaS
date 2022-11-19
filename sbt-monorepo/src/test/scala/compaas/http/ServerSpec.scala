package compaas.http

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.*
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.*
import scala.util.{Failure, Success}

class ServerRoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest:
  val testKit = ActorTestKit(
    ConfigFactory
      .parseString("""
      akka {
        loglevel = DEBUG
        http {
          server {
            hostname = 127.0.0.1
            port = 8080
          }
        }
      }
  """).withFallback(ConfigFactory.load())
  )
  import testKit.*
  given typedSystem: ActorSystem[?] = testKit.system

  val router = Router()

  val wsClient = WSProbe()

  "The server" should {
    "handle ws request at /@" in {
      WS("/@", wsClient.flow) ~> routes ~>
        check {
          // check response for WS Upgrade headers
          isWebSocketUpgrade shouldEqual true
        }
    }
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
