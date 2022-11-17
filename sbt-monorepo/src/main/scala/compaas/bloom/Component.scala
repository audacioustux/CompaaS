package compaas.bloom

import org.graalvm.polyglot.Source

import java.util.UUID

enum SupportedLanguage(val languageId: String) {
  case Js   extends SupportedLanguage("js")
  case Wasm extends SupportedLanguage("Wasm")
}

case class Component(
    id: UUID,
    language: SupportedLanguage,
    source: Source,
    name: String,
    description: Option[String]
)

object Component {
  def apply(name: String, description: Option[String], rawSource: String, languageId: String) = {
    val language = SupportedLanguage.values.find(_.languageId.equals(languageId)).get
    val source   = Source.newBuilder(languageId, rawSource, name).build()

    new Component(UUID.randomUUID(), language, source, name, description)
  }
}
