package compaas.core

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import compaas.core.{Component, Language}
import org.graalvm.polyglot.{Context, Engine, PolyglotException, Source, Value}

import scala.collection.JavaConverters.*
import scala.collection.mutable.{HashMap, HashSet}
import scala.util.{Failure, Success, Try}

import shared.Graal
object ComponentAgent:
  sealed trait Message
  protected final case class Dispatch(port: String, payload: String) extends Message

  def apply(component: Component): Behavior[Message] = Behaviors.setup { ctx =>
    import component.*

    language match {
      case Language.Js => {
        given graalCtx: Context = Context
          .newBuilder(language.languageId)
          .engine(Graal.engine)
          .allowExperimentalOptions(true)
          .option("js.esm-eval-returns-exports", "true")
          .build()

        val exports = graalCtx.eval(source)
        val ports = exports.getMemberKeys.asScala
          .map { port =>
            ctx.spawn(
              Behaviors.receiveMessage[String] { payload =>
                ctx.self ! Dispatch(port, payload)
                Behaviors.same
              },
              port
            )
          }
          .to(HashSet)

        (new ComponentAgent).dispatcher(exports)
      }
      case _ => ???
    }
  }

class ComponentAgent(using graalCtx: Context):
  import ComponentAgent.*

  def dispatcher(exports: Value): Behavior[Message] = Behaviors.setup { ctx =>
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
