package compaas.core

import compaas.core.Component.{Port, PortDirection}
import compaas.core.engine.*

import java.util.UUID

object Component {
  case class Manifest(
      name: String,
      version: String,
      description: String,
      ports: List[Port],
      modules: List[Module]
  )
  case class Port(name: String, direction: PortDirection)
  enum PortDirection:
    case In
    case Out

  def apply(manifest: Manifest): Component =
    val executor          = Executor.newBuilder().build()
    given ExecutorContext = executor.createContext(LanguageId.Js)

    new Component(manifest)
}

class Component(manifest: Component.Manifest)(using ec: ExecutorContext) {
  import manifest.*

  def invoke(portName: String, args: Any*): Any = {}
}
