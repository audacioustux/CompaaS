import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.graalvm.polyglot.*

enum SupportedLanguage(val languageId: String) {
  case Js   extends SupportedLanguage("js")
  case Wasm extends SupportedLanguage("Wasm")
}

object Component {
  private lazy val engine = Engine.newBuilder().build()

  def apply(name: String, rawSource: String, languageId: String) = {
    val language = SupportedLanguage.values.find(_.languageId.equals(languageId)).get
    val source   = Source.newBuilder(language.languageId, rawSource, name).build()

    new Component(source)
  }
}

class Component(source: Source) {
  import Component.*

  val context   = Context.newBuilder(source.getLanguage()).engine(engine).build()
  val sourceAST = context.parse(source)

  def init() = sourceAST.execute()
}