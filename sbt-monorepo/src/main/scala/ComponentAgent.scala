import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object ComponentAgent {
  sealed trait Protocol
  case class Message(msg: String) extends Protocol

  def apply(component: Component): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      Behaviors.same
    }
}

class ComponentAgent {}
