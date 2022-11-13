package http

import java.util.UUID

object Protocol {
  sealed trait In
  object In {
    case object Ping extends In
  }

  sealed trait Out
  object Out {
    case object Pong extends Out

    case object Completed                                 extends Out
    case class Failed(exceptionId: UUID, message: String) extends Out
  }
}
