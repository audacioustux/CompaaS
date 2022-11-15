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
    // TODO: add traceId
    case class Error(message: String) extends Out
  }
}
