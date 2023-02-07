import org.graalvm.polyglot.*;

class Main {

	public static class State {
		@HostAccess.Export
		public Integer count = 42;

		@HostAccess.Export
		public Value val = Value.asValue(count);

		@HostAccess.Export
		public void increment() {
			count++;
		}
	}

	public static void change(State counter) {
		counter.increment();
	}

	public static void main(String[] args) {
		Context context = Context.newBuilder().build();

		State state = new State();

		context.eval("js", "let state; (_state) => state = _state;").execute(state.val);
		context.eval("js", "state++");
		context.eval("js", "console.log(state);");
		// // context.eval("js", "console.log(Object.keys(state));");
		// context.eval("js", "console.log(state);");

		// change(state);

		System.out.println(state.val.asInt());
	}
}