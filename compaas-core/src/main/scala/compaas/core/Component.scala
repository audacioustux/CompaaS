package compaas.core

import akka.actor.typed.*
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.*

import java.util.UUID
import scala.collection.mutable.{HashMap, HashSet}

object ComponentAgent:
  sealed trait Command extends Serializable
  sealed trait Event   extends Serializable
  final case class State(ports: HashSet[Port] = HashSet(), connectors: HashSet[Connector] = HashSet())

  enum Direction:
    case In, Out
  case class Port(name: String, direction: Direction)
  case class Connector(connected: HashSet[Port])

  def apply(): Behavior[Command] = EventSourcedBehavior[Command, Event, State](
    persistenceId = PersistenceId.ofUniqueId("abc"),
    emptyState = State(),
    commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
    eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state")
  )

