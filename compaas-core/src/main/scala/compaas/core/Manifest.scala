package compaas.core

import compaas.core.engine.ModuleInfo

case class Manifest(
    name: String,
    version: String,
    description: String,
    ports: Ports,
    modules: List[ModuleInfo]
)

case class Port(name: String, direction: PortDirection)
enum PortDirection:
  case In
  case Out
