package compaas.bench.graal.polyglot

import org.graalvm.polyglot.*
import org.graalvm.polyglot.io.ByteSequence

import common.Graal

object NthPrimeMem {
  val modules = Map(
    "js" -> Source
      .newBuilder(
        "js",
        scala.io.Source
          .fromResource("js/nth-prime.mjs")
          .mkString,
        "nth-prime.mjs"
      )
      .mimeType("application/javascript+module")
      .build()
    // "wasm" -> Source
    //   .newBuilder(
    //     "wasm",
    //     ByteSequence.create(
    //       getClass.getClassLoader().getResourceAsStream("wasm/nth_prime.wasm").readAllBytes()
    //     ),
    //     "nth-prime.wasm"
    //   )
    //   .build()
  )

  def printMemoryUsage(label: String, numberOfInstances: Int): Unit = {
    System.gc()
    Thread.sleep(5000)
    val runtime    = Runtime.getRuntime()
    val usedMemory = runtime.totalMemory() - runtime.freeMemory()
    println(label + usedMemory / 1024 / 1024 + " mb")
    println(label + usedMemory / 1024 / numberOfInstances + " kb per instance")
  }

  def main(args: Array[String]): Unit = {
    val numberOfInstances = 10_000

    val contexts = (1 to numberOfInstances).map { _ =>
      Context
        .newBuilder()
        .engine(Graal.engine)
        .allowExperimentalOptions(true)
        .option("js.esm-eval-returns-exports", "true")
        .build()
    }

    val source = modules("js")

    val parsed = contexts.map { ctx => ctx.parse(source) }

    printMemoryUsage("after parse: ", numberOfInstances)

    val funcs = parsed.map { ast =>
      ast.execute().getMember("nth_prime")
    }

    funcs.foreach { f =>
      f.execute(10)
    }

    printMemoryUsage("after eval: ", numberOfInstances)

    // run randomly any func
    val random     = scala.util.Random
    val randomFunc = funcs(random.nextInt(funcs.length))
    println(randomFunc.execute(10))
  }
}
