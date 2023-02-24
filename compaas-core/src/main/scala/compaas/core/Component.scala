package compaas.core

import java.util.UUID

import compaas.core.engine.*

case class Ports(in: List[String], out: List[String])

object Component {
  def apply(manifest: Manifest): Component =
    val executor          = Executor.newBuilder().build()
    given ExecutorContext = executor.createContext(LanguageId.Js)

    new Component(manifest)
}

class Component(manifest: Manifest)(using ec: ExecutorContext) {
  val modules = manifest.modules.map(Module(_))
}
