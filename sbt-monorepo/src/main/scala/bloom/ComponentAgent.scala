package bloom

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.graalvm.polyglot.{Context, Engine, Source, Value}

object ComponentAgent {
  sealed trait Event
  case object Parse               extends Event
  case object Init                extends Event
  case class Send(portId: String) extends Event

  private lazy val engine = Engine.newBuilder().build()

  def apply(component: Component): Behavior[Event] = {
    import component.*
    given context: Context = Context
      .newBuilder(language.languageId)
      .engine(engine)
      .build()

    (new ComponentAgent).idle(source)
  }
}

class ComponentAgent()(using ctx: Context) {
  import ComponentAgent.*

  def initialized(evaledVal: Value): Behavior[Event] = Behaviors.receiveMessagePartial {
    case Send(portId) =>
      Behaviors.same
  }

  def parsed(parsedAST: Value): Behavior[Event] = Behaviors.receiveMessagePartial { case Init =>
    initialized(parsedAST.execute())
  }

  def idle(source: Source): Behavior[Event] = Behaviors.receiveMessagePartial { case Parse =>
    parsed(ctx.parse(source))
  }
}
