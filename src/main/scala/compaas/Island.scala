package compaas

import java.util.UUID

import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior

// import akka.NotUsed
// import akka.stream.scaladsl.Flow

object Island:

  sealed trait Command
  sealed trait Event
  final case class State()

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Island")

  def apply(id: UUID) = EventSourcedBehavior[Command, Event, State](
    persistenceId = PersistenceId.of(TypeKey.name, id.toString),
    emptyState = State(),
    commandHandler = (state, cmd) => Effect.none,
    eventHandler = (state, evt) => state,
  )
