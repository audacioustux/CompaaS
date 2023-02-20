package compaas.core.engine

enum LanguageId(val codename: String):
  case Js     extends LanguageId("js")
  case Python extends LanguageId("python")
  case Wasm   extends LanguageId("wasm")

case class LanguageInfo(val id: LanguageId)
