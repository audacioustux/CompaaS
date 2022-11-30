package compaas.http

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{Materializer, OverflowStrategy}
import compaas.http.Protocol.*

import scala.concurrent.ExecutionContext

private object Receptionist:
  sealed trait Command
  // TODO: add traceId
  final case class HandleMessage(msg: Either[Throwable, In]) extends Command
  case object Complete                                       extends Command
  final case class Fail(cause: Throwable)                    extends Command

  private def handle(msg: In)(using recipient: ActorRef[Out]) = msg match
    case In.Echo(msg) => recipient ! Out.Echo(msg)

  def apply(recipient: ActorRef[Out]) = Behaviors.setup[Command] { ctx =>
    Behaviors.receiveMessage {
      case HandleMessage(Right(msg)) =>
        handle(msg)(using recipient)
        Behaviors.same
      case HandleMessage(Left(e)) =>
        val errMsg = s"failed to parse message"
        ctx.log.error(errMsg, e)
        recipient ! Out.Error(errMsg)
        Behaviors.same
      case Complete =>
        Behaviors.stopped
      case Fail(cause) =>
        ctx.log.error("something went wrong", cause)
        Behaviors.stopped
    }
  }

object Sentry:
  sealed trait Command
  final case class SendNewSessionFlow(to: ActorRef[Flow[Either[Throwable, In], Out, ?]])
      extends Command

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    given Materializer     = Materializer(ctx)
    given ExecutionContext = ctx.executionContext

    Behaviors.receiveMessage { msg =>
      msg match
        case SendNewSessionFlow(to) =>
          val (recipient, source) = ActorSource
            .actorRef[Out](
              completionMatcher = PartialFunction.empty,
              failureMatcher = PartialFunction.empty,
              bufferSize = 1 << 6,
              // at-most-once delivery
              overflowStrategy = OverflowStrategy.dropHead
            )
            .preMaterialize()

          val receptionist = ctx.spawnAnonymous(Receptionist(recipient))
          val sink =
            import Receptionist.*

            ActorSink
              .actorRef(
                ref = receptionist,
                onCompleteMessage = Complete,
                onFailureMessage = (exception) => Fail(exception)
              )
              .contramap(HandleMessage(_))

          to ! Flow.fromSinkAndSourceCoupled(sink, source)

          Behaviors.same
    }
  }
