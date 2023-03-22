import org.graalvm.polyglot.*

@main def Main() = {
  val greeterJs: String = """
    
    export function greet(name) {
        return "Hello " + name + "!";
    }

    export const foo = "bar";

    let shutup_count = 0;

    export default (state, msg) => {
        const { count = 0 } = state;
        state.count = count + 1;

        if (count < 3) {
          return greet(msg)
        }

        shutup_count += 1;

        return shutup_count < 3 ? "Shut up!" : "I'm done!";
    }

    """

  val source  = Source.newBuilder("js", greeterJs, "greeter.mjs").build()
  val source2 = Source.newBuilder("js", greeterJs, "greeter2.mjs").build()

  val engine = Engine.create()
  val context = Context
    .newBuilder()
    .allowExperimentalOptions(true)
    .option("js.ecmascript-version", "2022")
    .option("js.esm-eval-returns-exports", "true")
    .option("js.strict", "true")
    .allowHostAccess(HostAccess.newBuilder().allowMapAccess(true).build())
    .engine(engine)
    .build()

  val exports  = context.eval(source)
  val exports2 = context.eval(source2)

  val state = new java.util.HashMap[String, Object]()

  println(exports.invokeMember("greet", "World"))

  (1 to 6).foreach { _ =>
    println(exports2.invokeMember("default", state, "World"))
    println(exports.invokeMember("default", state, "World"))
  }
}

// Output:
// Hello World!
// Hello World!
// Hello World!
// Hello World!
// Shut up!
// Shut up!
// Shut up!
// Shut up!
// I'm done!
// I'm done!
// I'm done!
// I'm done!
// I'm done!

// NOTE: not effected by gc
// NOTE: global vals (const, let, function, etc.) don't get affected by eval
