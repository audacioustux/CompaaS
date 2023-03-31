package compaas.core

import akka.actor.typed.*
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.*

import java.util.UUID
import scala.collection.mutable.{HashMap, HashSet}

object ComponentAgent:
  sealed trait Command                                                         extends Serializable
  sealed trait Event                                                           extends Serializable
  final case class State(ports: HashSet[Port], connectors: HashSet[Connector]) extends Serializable
  object State:
    def empty: State = State(HashSet.empty, HashSet.empty)

  enum Direction:
    case In, Out
  case class Port(id: UUID, direction: Direction)
  case class Connector(id: UUID, connected: HashSet[Port])

  def apply(): Behavior[Command] = EventSourcedBehavior[Command, Event, State](
    persistenceId = PersistenceId.ofUniqueId("abc"),
    emptyState = State.empty,
    commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
    eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state")
  )
