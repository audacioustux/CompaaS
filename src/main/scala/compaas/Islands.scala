package compaas

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.duration.DurationInt

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.util.Try

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model.ws.BinaryMessage
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives.*
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

import compaas.utils.http.Server
import compaas.utils.serde.Serializable

object Islands:
  final case class Event(message: Either[Throwable, String])

  def apply(ctx: ActorContext[?]) = ???

  // given system: ActorSystem[?] = ctx.system
  // given ec: ExecutionContext   = system.executionContext

  // val config = system.settings.config.getConfig("compaas.islands")

//   private def start(using system: ActorSystem[?], ec: ExecutionContext) = extractClientIP { ip =>
//     pathEndOrSingleSlash:
//       handleWebSocketMessages(
//         Flow[Message]
//           .filter {
//             case _: TextMessage =>
//               true
//             case bm: BinaryMessage =>
//               bm.dataStream.runWith(Sink.ignore);
//               false
//           }
//           .collectType[TextMessage]
//           .mapAsyncUnordered(16)(_.toStrict(3.seconds).map(_.text))
//           .map(ev => Try(readFromString[Event](ev)).toEither)
//           .via(???)
//           .map(writeToString(_))
//           .map[Message](TextMessage(_))
//       )

//   }

// end Islands

// class Islands(using system: ActorSystem[?], ec: ExecutionContext):
//   import Islands.{ *, given }

//   val config = system.settings.config.getConfig("compaas.islands")

//   val interface = config.getString("interface")
//   val port      = config.getInt("port")
