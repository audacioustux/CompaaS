import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.ByteSequence;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.graalvm.polyglot.proxy.*;
import java.util.*;

class Main {
	public static void main(String[] args) throws IOException {
		Engine engine = Engine.newBuilder().allowExperimentalOptions(true).option("js.webassembly", "true").build();
		Context context = Context.newBuilder().engine(engine).allowExperimentalOptions(true).allowAllAccess(true)
				.allowIO(true).build();
		context.initialize("wasm");
		context.initialize("js");

		ProxyObject state = ProxyObject.fromMap(new HashMap<String, Object>() {
			{
				put("wasmModule", Files.readAllBytes(Path.of("test.wasm")));
				put("count", 42);
			}
		});
		context.getBindings("js").putMember("state", state);

		Source testjs = Source.newBuilder("js", "load('./test.mjs')", "test.mjs")
				.mimeType("application/javascript+module").build();
		context.eval(testjs);

		Object count = state.getMember("count");
		System.out.println("count: " + count);
	}
}