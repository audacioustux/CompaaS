import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.model.*
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.ActorSink

import concurrent.duration.DurationInt

private object RootReceptionist {
  sealed trait Ack
  case object Ack extends Ack

  sealed trait Protocol
  case class Init(ackTo: ActorRef[Ack])                     extends Protocol
  case class Message(ackTo: ActorRef[Ack], msg: ws.Message) extends Protocol
  case object Complete                                      extends Protocol
  case class Fail(ex: Throwable)                            extends Protocol
}

object RootReceptionistFlow {
  import RootReceptionist.*

  def apply(receptionistActor: ActorRef[Protocol]): Flow[ws.Message, ws.Message, Any] = {
    val sink: Sink[ws.Message, NotUsed] = ActorSink.actorRefWithBackpressure(
      ref = receptionistActor,
      messageAdapter = (ackTo: ActorRef[Ack], msg) => Message(ackTo, msg),
      onInitMessage = (ackTo: ActorRef[Ack]) => Init(ackTo),
      ackMessage = Ack,
      onCompleteMessage = Complete,
      onFailureMessage = (exception) => Fail(exception)
    )
    val source = Source
      .tick(1.second, 1.second, "tick")
      .map(_ => ws.TextMessage(System.currentTimeMillis().toString + "\n"))

    Flow.fromSinkAndSource(sink, source)
  }
}

object RootReceptionistActor {
  import RootReceptionist.*

  def apply() = Behaviors.receive { (ctx, msg) =>
    msg match
      case Init(ackTo) =>
        ackTo ! Ack
        Behaviors.same
      case Message(ackTo, msg) =>
        println(s"Received message: $msg")
        ackTo ! Ack
        Behaviors.same
      case Complete =>
        Behaviors.stopped
      case Fail(ex) =>
        Behaviors.stopped
  }
}
