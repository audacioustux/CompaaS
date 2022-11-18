package compaas.bloom

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.graalvm.polyglot.{Context, Engine, PolyglotException, Source, Value}

import scala.util.{Failure, Success, Try}

object ComponentAgent:
  sealed trait Event

  sealed trait ParseEvent                                 extends Event
  case object Parsed                                      extends ParseEvent
  final case class ParseFailed(reason: PolyglotException) extends ParseEvent

  sealed trait Command                                  extends Event
  final case class Parse(replyTo: ActorRef[ParseEvent]) extends Command
  case object Init                                      extends Command
  final case class Send(portId: String)                 extends Command

  private lazy val engine = Engine.newBuilder().build()

  def apply(component: Component): Behavior[Event] =
    import component.*
    given ctx: Context = Context
      .newBuilder(language.languageId)
      .engine(engine)
      .build()

    (new ComponentAgent).idle(source)

class ComponentAgent()(using ctx: Context):
  import ComponentAgent.*

  def initialized(evaledVal: Value) = Behaviors.receiveMessagePartial[Event] { case Send(portId) =>
    Behaviors.same
  }

  def parsed(parsedAST: Value) = Behaviors.receiveMessagePartial[Event] { case Init =>
    initialized(parsedAST.execute())
  }

  def idle(source: Source) = Behaviors.receiveMessagePartial[Event] { case Parse(replyTo) =>
    Try(ctx.parse(source)) match
      case Success(parsedAST: Value) =>
        replyTo ! Parsed
        parsed(parsedAST)
      case Failure(exception: PolyglotException) =>
        replyTo ! ParseFailed(exception)
        Behaviors.same
      case Failure(exception) => throw exception
  }
