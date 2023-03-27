package compaas.core.engine

import org.graalvm.polyglot.*
import org.graalvm.polyglot.io.*

object Module:
  def apply(source: Source)(using ec: ExecutorContext): Module =
    new Module(source)

class Module(source: Source)(using ec: ExecutorContext):
  lazy val ast    = ec.parse(source)
  lazy val evaled = ast.execute()

  def execute(key: String, args: Any*) =
    val value = evaled.value.getMember(key)
    value.execute(args)
