import akka.NotUsed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.model.*
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{Attributes, CompletionStrategy}

import concurrent.duration.DurationInt

object RootReceptionist {
  sealed trait Message

  sealed trait IncommingMessage                                          extends Message
  sealed trait Response                                                  extends IncommingMessage
  case class ResponseMsg(msg: ws.Message)                                extends Response
  sealed trait SinkEvents                                                extends IncommingMessage
  final case class InMessage(ackTo: ActorRef[Ack.type], msg: ws.Message) extends SinkEvents
  case class Init(ackTo: ActorRef[Ack.type])                             extends SinkEvents
  case object Complete                                                   extends SinkEvents
  case class Fail(ex: Throwable)                                         extends SinkEvents
  sealed trait SourceEvents                                              extends IncommingMessage
  case object Ack                                                        extends SourceEvents

  sealed trait OutgoingMessage                 extends Message
  final case class OutMessage(msg: ws.Message) extends OutgoingMessage

  def apply(forwardTo: ActorRef[OutMessage]): Behavior[IncommingMessage] =
    Behaviors.withStash(100) { buffer =>
      Behaviors.setup { ctx =>
        Behaviors.receiveMessage {
          // sink events
          case InMessage(ackTo, msg) =>
            ctx.log.info("Incomming message: {}", msg)
            ackTo ! Ack
            Behaviors.same
          case Init(ackTo) =>
            ackTo ! Ack
            Behaviors.same
          case Complete =>
            Behaviors.stopped
          case Fail(ex) =>
            throw ex
          case Ack              => buffer.(Behaviors.same)
          case ResponseMsg(msg) => buffer.stash(ResponseMsg(msg)); Behaviors.same
        }
      }
    }
}
