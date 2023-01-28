package compaas.core

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Terminated}

import java.util.UUID

import compaas.core.Component
object ComponentManager:
  sealed trait Message
  final case class CreateComponent(component: Component) extends Message

  def apply() =
    Behaviors.setup { ctx =>
      ctx.log.info("ComponentManager started")

      Behaviors.receiveMessage {
        case CreateComponent(component) =>
          val agentId        = UUID.randomUUID()
          val componentAgent = ctx.spawn(ComponentAgent(component), agentId.toString)

          // TODO: remove this
          // componentAgent ! ComponentAgent.Dispatch("greet", "world")

          ctx.watch(componentAgent)
          ctx.log.info("ComponentAgent({}) created for Component({})", agentId, component.id)
          Behaviors.same
        case Terminated(componentAgent) =>
          ctx.log.info("ComponentAgent({}) terminated", componentAgent.path.name)
          Behaviors.same
      }
    }
