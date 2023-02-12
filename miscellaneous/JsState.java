import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.ByteSequence;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.graalvm.polyglot.proxy.*;
import java.util.*;

class Main {
	public static void main(String[] args) throws IOException {
		Engine engine = Engine.newBuilder().build();
		Context context = Context.newBuilder().engine(engine).build();
		// context.initialize("wasm");
		// context.initialize("js");

		ProxyObject state = ProxyObject.fromMap(new HashMap<String, Object>() {
			{
				// put("wasmModule", Files.readAllBytes(Path.of("test.wasm")));
				put("count", 42);
			}
		});

		context.eval("js", "let state; (s) => { state = s; }").execute(state);
		// context.getBindings("js").putMember("state", state);

		// load test.mjs as File
		String testmjs = Files.readString(Path.of("test.mjs"));

		// Source testjs = Source.newBuilder("js", testmjs,
		// "test.mjs").mimeType("application/javascript+module").build();
		context.eval("js", testmjs);

		Object count = state.getMember("count");
		System.out.println("count: " + count);
	}
}