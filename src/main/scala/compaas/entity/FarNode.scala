package compaas.entity

import concurrent.duration.DurationInt

import akka.actor.typed.*
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.RetentionCriteria

object FarNode:

  val typeKey = EntityTypeKey[Command]("FarNode")

  sealed trait Command               extends Serializable
  sealed trait Event                 extends Serializable
  final case class State(value: Int) extends Serializable

  private def handleCommands(state: State, command: Command): Effect[Event, State] =
    command match
      case _ =>
        Effect.none

  private def handleEvents(state: State, event: Event): State =
    event match
      case _ =>
        state

  def apply(nodeId: String): Behavior[Command] = EventSourcedBehavior[Command, Event, State](
    PersistenceId(typeKey.name, nodeId),
    State(0),
    commandHandler = handleCommands,
    eventHandler = handleEvents,
  ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))
    .onPersistFailure(
      SupervisorStrategy.restartWithBackoff(minBackoff = 10.seconds, maxBackoff = 60.seconds, randomFactor = 0.1)
    )

end FarNode
