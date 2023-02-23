package compaas.core

import java.util.UUID

import engine.ModuleInfo

case class ComponentInfo(
    val id: UUID,
    val name: String,
    val modules: List[ModuleInfo]
)

object Component {
  def apply(info: ComponentInfo): Component =
    new Component(info)
}

class Component(info: ComponentInfo) {
  import info.*
}
