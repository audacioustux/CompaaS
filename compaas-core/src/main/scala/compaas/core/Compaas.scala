package compaas.core

import org.graalvm.polyglot.*

object Compaas:
  def apply() =
    val engine  = GraalEngine()
    val context = engine.GraalContext()
    val source  = Source.newBuilder("js", "1 + 1", "test").build()
    val script  = context.parse(source)
    val result  = script.execute()
    println(result.asInt())
