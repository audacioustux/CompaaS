package compaas.core

import java.util.HashMap
import org.graalvm.polyglot.*

class StateProxy[K, V] extends HashMap[K, V] {
  override def get(key: Object): V      = super.get(key)
  override def put(key: K, value: V): V = super.put(key, value)
}
