package compaas.core

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.*

import java.util.UUID
import scala.collection.mutable.{HashMap, HashSet}

case class Module(name: String, language: String, code: String)

object Component:
  sealed trait Command               extends Serializable
  final case class Add(data: String) extends Command
  case object Clear                  extends Command

  sealed trait Event                   extends Serializable
  final case class Added(data: String) extends Event
  case object Cleared                  extends Event

  final case class State(ports: HashSet[Port] = HashSet(), connectors: HashSet[Connector] = HashSet(), module: Module)
      extends Serializable

  sealed trait Port
  final case class Inlet(id: UUID, name: Option[String])  extends Port
  final case class Outlet(id: UUID, name: Option[String]) extends Port

  sealed trait Connector
  final case class Hub(id: UUID, ports: HashSet[Port], name: Option[String]) extends Connector

  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match
      case Add(data) => Effect.persist(Added(data))
      case Clear     => Effect.persist(Cleared)
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match
      case Added(data) => state
      case Cleared     => state
  }

  def apply(name: String, language: String, code: String): Behavior[Command] = Behaviors.setup { ctx =>
    val persistenceId = PersistenceId.ofUniqueId(UUID.randomUUID().toString)
    EventSourcedBehavior[Command, Event, State](
      persistenceId = persistenceId,
      emptyState = State(module = Module(name, language, code)),
      commandHandler,
      eventHandler
    )
  }
