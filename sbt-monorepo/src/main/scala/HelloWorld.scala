import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object HelloWorld {

  // Command
  sealed trait Command                                                  extends CborSerializable
  final case class Greet(whom: String)(val replyTo: ActorRef[Greeting]) extends Command
  // Response
  final case class Greeting(whom: String, numberOfPeople: Int) extends CborSerializable

  // Event
  final case class Greeted(whom: String) extends CborSerializable

  // State
  final case class KnownPeople(names: Set[String]) extends CborSerializable {
    def add(name: String): KnownPeople = copy(names = names + name)

    def numberOfPeople: Int = names.size
  }

  private val commandHandler: (KnownPeople, Command) => Effect[Greeted, KnownPeople] = { (_, cmd) =>
    cmd match {
      case cmd: Greet => greet(cmd)
    }
  }

  private def greet(cmd: Greet): Effect[Greeted, KnownPeople] =
    Effect
      .persist(Greeted(cmd.whom))
      .thenRun(state => cmd.replyTo ! Greeting(cmd.whom, state.numberOfPeople))

  private val eventHandler: (KnownPeople, Greeted) => KnownPeople = { (state, evt) =>
    state.add(evt.whom)
  }

  val TypeKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("HelloWorld")

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.info("Starting HelloWorld {}", entityId)
      EventSourcedBehavior(
        persistenceId,
        emptyState = KnownPeople(Set.empty),
        commandHandler,
        eventHandler
      )
    }
  }

}
