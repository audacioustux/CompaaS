package compaas.core

import akka.persistence.typed.scaladsl.*
import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.persistence.typed.*

import java.util.UUID
import scala.collection.mutable.HashMap

object Component:
  sealed trait Command                                extends Serializable
  sealed trait Event                                  extends Serializable
  final case class State(kv: HashMap[String, String]) extends Serializable

  val commandHandler: (State, Command) => Effect[Event, State] =
    (state, command) =>
      command match
        case _ =>
          Effect.none

  val eventHandler: (State, Event) => State =
    (state, event) =>
      event match
        case _ =>
          state

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    val persistenceId = PersistenceId.ofUniqueId(UUID.randomUUID().toString())
    EventSourcedBehavior[Command, Event, State](
      persistenceId,
      State(HashMap.empty),
      commandHandler,
      eventHandler,
    )
  }

end Component
