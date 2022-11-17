package scalatest

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.{ActorRef, Scheduler}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.*
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.{ByteString, Timeout}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.*
import scala.util.{Failure, Success}

import Directives.*

class AkkaHttpWebSocketExample extends AnyWordSpec with Matchers with ScalatestRouteTest {
  val wsClient = WSProbe()

  "The service" should {
    "greet" in {
      WS("/greeter", wsClient.flow) ~> websocketRoute ~>
        check {
          // check response for WS Upgrade headers
          isWebSocketUpgrade shouldEqual true

          // manually run a WS conversation
          wsClient.sendMessage("Peter")
          wsClient.expectMessage("Hello Peter!")

          wsClient.sendMessage(BinaryMessage(ByteString("abcdef")))
          wsClient.expectNoMessage(100.millis)

          wsClient.sendMessage("John")
          wsClient.expectMessage("Hello John!")

          wsClient.sendCompletion()
          wsClient.expectCompletion()
        }
    }
  }

  def greeter: Flow[Message, Message, Any] =
    Flow[Message].mapConcat {
      case tm: TextMessage =>
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }

  val websocketRoute =
    path("greeter") {
      handleWebSocketMessages(greeter)
    }
}
