package compaas.core.engine

import org.graalvm.polyglot.*

import java.io.File
import java.util.UUID

case class ModuleInfo(source: Source, language: LanguageInfo)

object Module {
  def apply(info: ModuleInfo)(using ExecutorContext): Module =
    new Module(info)
}

class Module(info: ModuleInfo)(using ec: ExecutorContext) {
  private val initValue: Value = ec.initialize(info.source)
  val exports = {
    info.language.id match {
      case LanguageId.Js => initValue
      case _             => ???
    }
  }
}
