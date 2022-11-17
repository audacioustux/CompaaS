package compaas.http

import java.util.UUID

object Protocol {
  sealed trait In
  object In {
    case class Echo(msg: String) extends In
  }

  sealed trait Out
  object Out {
    case class Echo(msg: String) extends Out
    // TODO: add traceId
    case class Error(message: String) extends Out
  }
}
