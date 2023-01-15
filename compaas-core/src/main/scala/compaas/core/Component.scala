package compaas.core

import org.graalvm.polyglot.Source

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

import java.util.UUID

enum Language(val languageId: String):
  case Js   extends Language("js")
  case Wasm extends Language("Wasm")

final case class Component(
    id: UUID,
    language: Language,
    source: Source,
    name: String,
    description: Option[String]
)

object Component:
  def apply(language: Language, source: Source, name: String, description: Option[String] = None) =
    new Component(UUID.randomUUID(), language, source, name, description)

  def fromJson(json: String): Component =
    final case class ComponentJson(
        languageId: String,
        code: String,
        name: String,
        description: Option[String]
    )
    implicit val codec: JsonValueCodec[ComponentJson] = JsonCodecMaker.make
    val componentJson                                 = readFromArray[ComponentJson](json.getBytes)

    import componentJson.*
    val language = Language.values.find(_.languageId.equals(languageId)).get
    val source   = Source.newBuilder(languageId, code, name).build()

    Component(language, source, name, description)
