package compaas.utils.http

import concurrent.duration.DurationInt

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.model.ws.{ BinaryMessage, Message, TextMessage }
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Sink, Source }

object WebsocketRoute:
  // how many concurrent streamed messages can be processed
  val concurrency = Runtime.getRuntime.availableProcessors
  // how long to wait for a strict message before giving up
  val timeout = 3.seconds

  def apply(source: Source[String, ?], sink: Sink[String, ?])(using Materializer, ExecutionContext): Route =
    handleWebSocketMessages(
      Flow[Message]
        .filter {
          case _: TextMessage =>
            true
          case bm: BinaryMessage =>
            bm.dataStream.runWith(Sink.ignore);
            false
        }
        .collectType[TextMessage]
        .mapAsyncUnordered(concurrency)(_.toStrict(timeout).map(_.text))
        .via(Flow.fromSinkAndSource(sink, source))
        .map[Message](TextMessage(_))
    )
