package compaas.core

import java.util.UUID

import engine.{Module}

case class Ports(in: List[String], out: List[String])

object Component {
  def apply(modules: List[Module], ports: Ports): Component =
    new Component()
}

class Component() {}
