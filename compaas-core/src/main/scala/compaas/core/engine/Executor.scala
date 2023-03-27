package compaas.core.engine

import org.graalvm.polyglot.*

object Executor:
  def newBuilder() = new Builder
  class Builder:
    private val engineBuilder = Engine.newBuilder()

    def allowExperimentalOptions(enable: Boolean): Builder =
      engineBuilder.allowExperimentalOptions(enable); this

    def spawnIsolate(language: Language): Builder =
      engineBuilder.option("engine.SpawnIsolate", language.getId()); this

    def isolateMaxHeapSize(bytes: Int): Builder =
      engineBuilder.option("engine.IsolateOption.MaxHeapSize", s"${bytes / 1024 / 1024}m"); this

    def memoryProtection(enable: Boolean): Builder =
      engineBuilder.option("engine.MemoryProtection", enable.toString()); this

    def build(): Executor =
      given engine: Engine = engineBuilder.build()
      new Executor

class Executor(using engine: Engine):
  def newContextBuilder() = ExecutorContext.newBuilder()
