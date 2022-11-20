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
import compaas.http.Protocol.{In, Out}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.*
import scala.util.{Failure, Success}

class RoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest:
  val testKit = ActorTestKit()
  import testKit.*
  given typedSystem: ActorSystem[?] = testKit.system

  val probe = createTestProbe[Route]()

  spawn(Behaviors.setup { ctx =>
    val sentry = ctx.spawn(Sentry(), "sentry")
    val routes = Routes(sentry)(using ctx)

    probe ! routes

    Behaviors.empty
  })

  val routes = probe.expectMessageType[Route]

  "path is `/@`" should {
    "handle with websocket handler" in {
      val wsClient = WSProbe()
      WS("/@", wsClient.flow) ~> routes ~> check {
        isWebSocketUpgrade shouldEqual true
      }
    }
  }
  "path is `/@/`" should {
    "handle with websocket handler" in {
      val wsClient = WSProbe()
      WS("/@/", wsClient.flow) ~> routes ~> check {
        isWebSocketUpgrade shouldEqual true
      }
    }
  }

  "/@ handler" should {
    "echo back" in {
      val wsClient = WSProbe()
      WS("/@/", wsClient.flow) ~> routes ~> check {
        val msg = """{"type":"Echo","msg":"hello"}"""

        wsClient.sendMessage(msg)
        wsClient.expectMessage(s"$msg")
      }
    }
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
