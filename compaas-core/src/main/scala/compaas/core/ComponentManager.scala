package compaas.core

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Terminated}

import java.util.UUID

object ComponentManager:
  sealed trait Message
  final case class CreateComponent(component: Component) extends Message

  def apply() =
    Behaviors.setup { ctx =>
      ctx.log.info("ComponentManager started")

      Behaviors.receiveMessage {
        case CreateComponent(component) =>
          val componentAgent = ctx.spawn(ComponentAgent(component), component.id.toString)

          // TODO: remove this
          // componentAgent ! ComponentAgent.Dispatch("greet", "world")

          ctx.watch(componentAgent)
          ctx.log.info("ComponentAgent created for component {}", component.id)
          Behaviors.same
        case Terminated(componentAgent) =>
          ctx.log.info("ComponentAgent terminated for component {}", componentAgent.path.name)
          Behaviors.same
      }
    }
