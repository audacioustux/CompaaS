import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.ByteSequence;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.graalvm.polyglot.proxy.*;
import java.util.*;

class Main {
	public static void main(String[] args) throws IOException, NoSuchMethodException {
		Map<String, Object> state = new HashMap<String, Object>() {
			{
				put("count", 42);
			}
		};

		String code = """
				export const increment = (state) => { state.count++; };
				export const add_member = (state) => { state.foo = 'bar'; };
				export const add_closure = (state) => () => { state.count++; };

				class Rectangle {
					constructor(height, width) {
						this.height = height;
						this.width = width;
					}
					// Getter
					get area() {
						return this.calcArea();
					}
					// Method
					calcArea() {
						return this.height * this.width;
					}
					// Generator
					*getSides() {
						yield this.height;
						yield this.width;
						yield this.height;
						yield this.width;
					}
				}

				export const add_rect = (state) => state.rect = new Rectangle(10, 10);
				export const get_rect_area = (state) => state.rect.calcArea();
				""";

		Source source = Source.newBuilder("js", code, "test.mjs").mimeType("application/javascript+module").build();

		Engine engine = Engine.newBuilder().build();

		HostAccess.Builder builder = HostAccess.newBuilder();
		// https://github.com/oracle/graaljs/issues/143
		builder.allowMapAccess(true);

		{
			Context context = Context.newBuilder().allowExperimentalOptions(true)
					.option("js.esm-eval-returns-exports", "true").allowHostAccess(builder.build()).engine(engine)
					.build();

			Value exports = context.eval(source);

			exports.invokeMember("increment", state);
			System.out.println("count: " + state.get("count"));

			exports.invokeMember("add_member", state);
			System.out.println("foo: " + state.get("foo"));

			exports.invokeMember("add_closure", state).execute();
			System.out.println("count: " + state.get("count"));

			exports.invokeMember("add_rect", state);
			Value rect_area = exports.invokeMember("get_rect_area", state);
			System.out.println("rect area: " + rect_area);

			// context.close();
		}

		try (Context context = Context.newBuilder().allowExperimentalOptions(true)
				.option("js.esm-eval-returns-exports", "true").allowHostAccess(builder.build()).engine(engine)
				.build()) {
			Value exports = context.eval(source);

			Value rect_area = exports.invokeMember("get_rect_area", state);
			System.out.println("rect area: " + rect_area);
		}
	}
}