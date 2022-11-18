package compaas.http

import java.util.UUID

object Protocol:
  sealed trait In
  object In:
    final case class Echo(msg: String) extends In

  sealed trait Out
  object Out:
    final case class Echo(msg: String) extends Out
    // TODO: add traceId
    final case class Error(message: String) extends Out
