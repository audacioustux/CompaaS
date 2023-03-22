package compaas.core.engine

import org.graalvm.polyglot.*
import org.graalvm.polyglot.io.*

import java.io.File
import java.util.UUID

import collection.convert.ImplicitConversionsToScala.*

object Module {
  case class Manifest(source: Source, language: LanguageInfo):
    def this(source: Array[Byte], language: LanguageInfo, name: String) =
      this(
        Source.newBuilder(language.id.codename, ByteSequence.create(source), name).build(),
        language
      )

  def apply(manifest: Manifest)(using ExecutorContext): Module =
    new Module(manifest)
}

class Module(manifest: Module.Manifest)(using ec: ExecutorContext) {
  import manifest.*

  private val initValue: Value = ec.initialize(source)
  private val exports = language.id match {
    case LanguageId.Js => initValue
    case _             => ???
  }
}
