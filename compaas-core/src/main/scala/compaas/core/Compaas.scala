package compaas.core

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors

object Compaas:
  def apply(): Behavior[Unit] =
    Behaviors.setup { ctx =>
      ctx.log.info("Starting compaas")

      val component = ctx.spawn(Component("test", "js", "console.log('hello world')"), "test")

      component ! Component.Add("test")

      Behaviors.receiveSignal { case (_, PostStop) =>
        ctx.log.info("Stopping compaas")
        Behaviors.same
      }
    }
