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
		Context context = Context.newBuilder().engine(engine).option("wasm.Builtins", "wasi_snapshot_preview1")
				.allowAllAccess(true).build();
		context.initialize("wasm");
		context.initialize("js");

		ProxyObject state = ProxyObject.fromMap(new java.util.HashMap<String, Object>());
		context.getBindings("js").putMember("state", state);

		// source from test.mjs in this directory
		Path path = Path.of("test.mjs");
		String data = Files.readString(path);
		Source testjs = Source.newBuilder("js", data, "test.mjs").build();
		context.eval(testjs);
	}
}