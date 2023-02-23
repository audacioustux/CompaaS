package compaas.core.engine

import org.graalvm.polyglot.*
import java.io.File
import java.util.UUID

case class ModuleInfo(id: UUID, source: Source, language: LanguageInfo)

object Module {
  def apply(info: ModuleInfo, ec: ExecutorContext): Module =
    new Module(info)(using ec)
}

class Module(info: ModuleInfo)(using ec: ExecutorContext) {}
