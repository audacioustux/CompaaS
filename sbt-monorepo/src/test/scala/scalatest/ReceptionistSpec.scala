// import akka.actor.testkit.typed.scaladsl.TestProbe
// import akka.actor.typed.scaladsl.AskPattern.*
// import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
// import akka.http.scaladsl.model.StatusCodes
// import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
// import akka.http.scaladsl.server.Directives.*
// import akka.http.scaladsl.server.*
// import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
// import akka.stream.scaladsl.{Flow, Sink, Source}
// import akka.util.{ByteString, Timeout}
// import org.scalatest.matchers.should.Matchers
// import org.scalatest.wordspec.AnyWordSpec

// import scala.concurrent.duration.*
// import scala.util.{Failure, Success}

// class ReceptionistSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {
//   import akka.actor.typed.scaladsl.adapter.*
//   given ActorSystem[Nothing] = system.toTyped

//   val wsClient       = WSProbe()
//   val routeUnderTest = new Routes()

//   "The service" should {
//     "greet" in {
//       WS("/", wsClient.flow) ~> routeUnderTest.theJobRoutes ~>
//         check {
//           // check response for WS Upgrade headers
//           isWebSocketUpgrade shouldEqual true

//           // manually run a WS conversation
//           wsClient.sendMessage("Peter")
//           wsClient.expectMessage("Hello Peter!")

//           wsClient.sendMessage(BinaryMessage(ByteString("abcdef")))
//           wsClient.expectNoMessage(100.millis)

//           wsClient.sendMessage("John")
//           wsClient.expectMessage("Hello John!")

//           wsClient.sendCompletion()
//           wsClient.expectCompletion()
//         }
//     }
//   }
// }
