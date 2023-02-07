import org.graalvm.polyglot.*;

class Main {
	public static class JavaRecord {
		@HostAccess.Export
		public int x;

		@HostAccess.Export
		public String name() {
			return "foo";
		}
	}

	public static void main(String[] args) {
		Context context = Context.newBuilder().build();

		JavaRecord record = new JavaRecord();
		context.getBindings("js").putMember("javaRecord", record);

		context.eval("js", "const record = javaRecord; const change = (n) => record.x = n;");

		context.eval("js", "change(2)");

		System.out.println(record.x);

		Value foo = context.eval("js", "change;");

		foo.execute(3);

		System.out.println(record.x);
	}
}