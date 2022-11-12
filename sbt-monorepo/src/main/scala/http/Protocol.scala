package http

object Protocol {
  sealed trait In
  object In {
    case class Request(data: String) extends In
  }

  sealed trait Out
  object Out {
    case class Response(data: String) extends Out
  }

  sealed trait Ack extends Out
  case object Ack  extends Ack
}
