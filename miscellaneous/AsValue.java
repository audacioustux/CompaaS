import org.graalvm.polyglot.*;

class Main {
	public static class JavaRecord {
		@HostAccess.Export
		public int x = 42;

		// @HostAccess.Export
		// public Object x = new Object();

		@HostAccess.Export
		public String name() {
			return "foo";
		}
	}

	public static void main(String[] args) {
		Context context = Context.newBuilder().create();
		Value record = context.asValue(new JavaRecord());
		record.getMember("x").asInt();
		record.getMember("name").execute().asString();

		context.eval("js", "(record) => record.x").execute(record).asInt();
		// context.eval("js", "(record) => record.putMember('y',
		// 3)").execute(record).asInt();
		context.eval("js", "(record) => record.name()").execute(record).asString();

		// System.out.println(record.getMember("y").asInt());

		// Context context = Context.create();
		// // Value record = context.asValue(new JavaRecord());
		// Value record = context.asValue(new JavaRecord());

		// context.eval("js", "let state; (_state) => state = _state;").execute(record);
		// context.eval("js", "console.log(state.name());");

		// System.out.println(record.getMember("x").asInt() == 42);
		// System.out.println(record.getMember("name").execute().asString().equals("foo"));

		// // assert context.eval("js", "(function(record)
		// // record.x)").execute(record).asInt() == 42;
		// // assert context.eval("js", "(function(record)
		// // record.y)").execute(record).asDouble() == 42.0d;
		// // assert context.eval("js", "(function(record)
		// // record.name())").execute(record).asString().equals("foo");

		// System.out.println(context.eval("js", "(record) =>
		// record.name()").execute(record).asString().equals("foo"));
	}
}