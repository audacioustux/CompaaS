package compaas

import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.Behaviors

object Guardian:
  def apply() = Behaviors.setup[Nothing] { ctx =>
    ctx.log.info("Starting up...")
    Behaviors.receiveSignal { case (_, PostStop) =>
      ctx.log.info("Shutting down...")
      Behaviors.same
    }
  }
