package compaas.core

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors

object Compaas:
  def apply(): Behavior[Unit] =
    Behaviors.setup { ctx =>
      Behaviors.empty

    }
