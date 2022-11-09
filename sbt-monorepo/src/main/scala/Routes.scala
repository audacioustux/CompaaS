import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.CompletionStrategy
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}

object Routes {
  def apply(ctx: ActorContext[?], receptionist: ActorRef[ServiceReceptionist.Protocol])(using
      ActorSystem[?]
  ): Route = {
    import ServiceReceptionist.{Ask, Message}

    sealed trait Ack
    object Ack extends Ack

    sealed trait SourceProtocol
    case object Complete           extends SourceProtocol
    case class Fail(ex: Throwable) extends SourceProtocol

    val ackTo = ctx.spawnAnonymous(
      // TODO: handle backpressure properly
      Behaviors.receiveMessage { case Ack => Behaviors.same }
    )

    val (customer, outgoing: Source[ws.Message, NotUsed]) = ActorSource
      .actorRefWithBackpressure(
        ackTo,
        ackMessage = Ack,
        completionMatcher = { case Complete => CompletionStrategy.draining },
        failureMatcher = { case Fail(ex) => ex }
      )
      .collect {
        case Message(msg) => { ws.TextMessage(msg) }
      }
      .toMat(BroadcastHub.sink[ws.Message](1 << 8))(Keep.both)
      .run()

    val incoming: Sink[ws.Message, NotUsed] = {
      trait SinkProtocol
      case class Init(ackTo: ActorRef[Ack])                       extends SinkProtocol
      case class WsMessage(ackTo: ActorRef[Ack], msg: ws.Message) extends SinkProtocol
      case object Complete                                        extends SinkProtocol
      case class Fail(ex: Throwable)                              extends SinkProtocol

      ActorSink.actorRefWithBackpressure(
        ref = ctx.spawnAnonymous(Behaviors.withStash(10) { buffer =>
          Behaviors.setup { ctx =>
            Behaviors.receiveMessage {
              case WsMessage(ackTo, msg) =>
                receptionist ! Ask(customer, msg.asTextMessage.getStrictText)
                // TODO: handle backpressure properly
                ackTo ! Ack
                Behaviors.same
              case Complete    => Behaviors.stopped
              case Fail(ex)    => throw ex
              case Init(ackTo) => ackTo ! Ack; Behaviors.same
            }
          }
        }),
        messageAdapter = (ackTo: ActorRef[Ack], msg) => WsMessage(ackTo, msg),
        onInitMessage = (ackTo: ActorRef[Ack]) => Init(ackTo),
        ackMessage = Ack,
        onCompleteMessage = Complete,
        onFailureMessage = (exception) => Fail(exception)
      )
    }

    pathSingleSlash {
      handleWebSocketMessages(
        Flow.fromSinkAndSource(incoming, outgoing)
      )
    }
  }
}
