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
		Context context = Context.newBuilder().allowExperimentalOptions(true)
				.option("js.esm-eval-returns-exports", "true").engine(engine).build();

		ProxyObject state = ProxyObject.fromMap(new HashMap<String, Object>() {
			{
				put("count", 42);
			}
		});

		String code = """
				let state;
				export const init = (s) => { state = s; }
				export const increment = () => state.count++;
				""";

		Source source = Source.newBuilder("js", code, "test.mjs").mimeType("application/javascript+module").build();

		Value exports = context.eval(source);

		exports.invokeMember("init", state);
		exports.invokeMember("increment");

		Object count = state.getMember("count");
		System.out.println("count: " + count);
	}
}