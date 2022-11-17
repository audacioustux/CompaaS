package compaas.bloom

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.graalvm.polyglot.{Context, Engine, Source, Value}

object ComponentAgent {
  sealed trait Event

  sealed trait Command            extends Event
  case object Parse               extends Command
  case object Init                extends Command
  case class Send(portId: String) extends Command

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

  def initialized(evaledVal: Value) = Behaviors.receiveMessagePartial[Event] { case Send(portId) =>
    Behaviors.same
  }

  def parsed(parsedAST: Value) = Behaviors.receiveMessagePartial[Event] { case Init =>
    initialized(parsedAST.execute())
  }

  def idle(source: Source) = Behaviors.receiveMessagePartial[Event] { case Parse =>
    parsed(ctx.parse(source))
  }
}
