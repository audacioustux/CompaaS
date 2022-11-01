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
    sealed trait Ack
    object Ack extends Ack

    sealed trait Protocol
    case object Complete           extends Protocol
    case class Fail(ex: Throwable) extends Protocol

    val ackTo = ctx.spawnAnonymous(Behaviors.setup { context =>
      Behaviors.receiveMessage { case Ack =>
        Behaviors.same
      }
    })

    val (customer, outgoing: Source[ws.Message, NotUsed]) = ActorSource
      .actorRefWithBackpressure(
        ackTo,
        ackMessage = Ack,
        completionMatcher = { case Complete => CompletionStrategy.draining },
        failureMatcher = { case Fail(ex) => ex }
      )
      .collect {
        case ServiceReceptionist.Message(msg) => { ws.TextMessage(msg) }
      }
      .toMat(BroadcastHub.sink[ws.Message](1 << 8))(Keep.both)
      .run()

    val incoming: Sink[ws.Message, NotUsed] = {
      trait Protocol
      case class Init(ackTo: ActorRef[Ack])                     extends Protocol
      case class Message(ackTo: ActorRef[Ack], msg: ws.Message) extends Protocol
      case object Complete                                      extends Protocol
      case class Fail(ex: Throwable)                            extends Protocol

      ActorSink.actorRefWithBackpressure(
        ref = ctx.spawnAnonymous(Behaviors.withStash(10) { buffer =>
          Behaviors.setup { ctx =>
            Behaviors.receiveMessage {
              case Message(ackTo, msg) =>
                ackTo ! Ack
                receptionist ! ServiceReceptionist
                  .Ask(customer, msg.asTextMessage.getStrictText)
                Behaviors.same
              case Complete    => Behaviors.stopped
              case Fail(ex)    => throw ex
              case Init(ackTo) => ackTo ! Ack; Behaviors.same
            }
          }
        }),
        messageAdapter = (ackTo: ActorRef[Ack], msg) => Message(ackTo, msg),
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
