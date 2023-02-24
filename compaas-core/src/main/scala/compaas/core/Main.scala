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

  val executor          = Executor.newBuilder().build()
  given ExecutorContext = executor.createContext(LanguageId.Js)

  val modules = List(
    ModuleInfo(
      Source.newBuilder("js", greeterJs, "greeter.js").build(),
      LanguageInfo(LanguageId.Js)
    )
  ).map(Module(_))

  val component = Component(modules, ports)
}
