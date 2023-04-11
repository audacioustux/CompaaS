package compaas.core

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.*

import java.util.UUID
import scala.collection.mutable.{HashMap, HashSet}

object Component:
  sealed trait Command extends Serializable
  sealed trait Event   extends Serializable
  final case class State(ports: HashSet[Port] = HashSet(), connectors: HashSet[Connector] = HashSet())

  sealed trait Port
  final case class Inlet(id: UUID, name: Option[String])  extends Port
  final case class Outlet(id: UUID, name: Option[String]) extends Port

  sealed trait Connector
  final case class Hub(id: UUID, ports: HashSet[Port], name: Option[String]) extends Connector

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    val persistenceId = PersistenceId.ofUniqueId(UUID.randomUUID().toString)
    EventSourcedBehavior[Command, Event, State](
      persistenceId = persistenceId,
      emptyState = State(),
      commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }
