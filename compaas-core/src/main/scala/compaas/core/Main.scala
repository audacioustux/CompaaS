package compaas.core

import org.graalvm.polyglot.*

import java.nio.file.{Files, Paths}
import compaas.core.engine.ModuleInfo
import compaas.core.engine.Module
import compaas.core.engine.LanguageId
import compaas.core.engine.LanguageInfo
import compaas.core.engine.Executor
import compaas.core.engine.ExecutorContext

@main def Main() = {
  val greeterJs: String = """
    
    function greet(name) {
        return "Hello " + name + "!";
    }
    
    """

  val ports = Ports(List("greet"), List("greet"))

  val moduleInfos = List(
    ModuleInfo(
      Source.newBuilder("js", greeterJs, "greeter.mjs").build(),
      LanguageInfo(LanguageId.Js)
    )
  )

  val manifest = Manifest("test", "0.0.1", "test", ports, moduleInfos)

  val component = Component(manifest)
}
