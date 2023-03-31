package compaas.core

import org.graalvm.polyglot.*
import org.graalvm.polyglot.io.*

case class ModuleSpec(language: String, code: CharSequence, name: String, metadata: Map[String, Any] = Map.empty)

object Module:
  def apply(spec: ModuleSpec)(using GraalEngine#GraalContext) =
    import spec.*

    val source: Source =
      val builder = Source.newBuilder(language, code, name)

      metadata.get("standard").map { case ("ecmascript", version) =>
        builder.mimeType("application/javascript+module")
      }

      builder.build()

    new Module(source)

class Module(source: Source)(using ec: GraalEngine#GraalContext):
  lazy val ast     = ec.parse(source)
  lazy val exports = ast.execute()

  println(s"Module exports: ${exports.getMemberKeys()}")
