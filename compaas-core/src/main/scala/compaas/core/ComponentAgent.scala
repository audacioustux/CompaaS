package compaas.core

import akka.actor.typed.pubsub.Topic.Publish
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.graalvm.polyglot.{Context, Engine, PolyglotException, Source, Value}
import scala.collection.mutable
import java.util.LinkedList
import scala.util.{Failure, Success, Try}

import shared.Graal
import akka.actor.typed.Signal
import akka.actor.typed.PostStop
import java.util.HashSet
import akka.actor.typed.pubsub.Topic.Subscribe

object ComponentAgent:
  sealed trait Message
  protected final case class Dispatch(port: String, payload: String) extends Message

  def apply(component: Component): Behavior[Message] =
    import component.*

    val builder = Context.newBuilder(language.languageId).engine(Graal.engine)

    language match {
      case Language.Js => {
        given graalCtx: Context = builder
          .allowExperimentalOptions(true)
          .option("js.esm-eval-returns-exports", "true")
          .build()
        val exports = graalCtx.eval(source)

        (new ComponentAgent).js(exports)
      }
      case Language.Wasm => {
        given graalCtx: Context = builder.build()
        graalCtx.eval(source)
        val exports = graalCtx.getBindings("wasm").getMemberKeys()
        ???
      }
    }

class ComponentAgent()(using graalCtx: Context):
  import ComponentAgent.*

  val ports = mutable.HashSet[ActorRef[?]]()

  def js(exports: Value): Behavior[Message] = Behaviors.setup { ctx =>
    Behaviors
      .receiveMessage { case Dispatch(port, payload) =>
        val result = Try(exports.getMember(port).execute(payload))
        result match {
          case Success(value)     => println(value)
          case Failure(exception) => println(exception)
        }
        Behaviors.same
      }
      .receiveSignal { case (_, PostStop) =>
        graalCtx.close()
        Behaviors.same
      }
      .narrow
  }
