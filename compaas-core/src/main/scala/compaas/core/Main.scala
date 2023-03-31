package compaas.core

@main def start(): Unit =
  val engine     = GraalEngine.newBuilder().allowExperimentalOptions(true).build()
  val moduleSpec = ModuleSpec("js", "export const foo = 42", "test.js")
