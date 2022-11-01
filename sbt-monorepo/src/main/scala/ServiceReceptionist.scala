import akka.NotUsed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.model.*
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{Attributes, CompletionStrategy}

import concurrent.duration.DurationInt

object ServiceReceptionist {
  sealed trait Protocol
  case class Message(msg: String)                         extends Protocol
  case class Ask(replyTo: ActorRef[Message], msg: String) extends Protocol

  def apply(): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Message(msg) =>
          ctx.log.error(msg)
          Behaviors.same
        case Ask(replyTo, msg) => replyTo ! Message("Hello " + msg); Behaviors.same
      }
    }
}
