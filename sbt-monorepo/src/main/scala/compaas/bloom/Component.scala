package compaas.bloom

import org.graalvm.polyglot.Source

import java.util.UUID

enum Language(val languageId: String) {
  case Js   extends Language("js")
  case Wasm extends Language("Wasm")
}

final case class Component(
    id: UUID,
    language: Language,
    source: Source,
    name: String,
    desc: Option[String]
)

object Component {
  def apply(languageId: String, rawSource: String, name: String, desc: Option[String] = None) = {
    val language = Language.values.find(_.languageId.equals(languageId)).get
    val source   = Source.newBuilder(languageId, rawSource, name).build()

    new Component(UUID.randomUUID(), language, source, name, desc)
  }
}
