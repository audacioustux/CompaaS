import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.ByteSequence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class Main {
	public static void main(String[] args) throws IOException {
		byte[] binary = Files.readAllBytes(Path.of("./string-reverse.wasm"));
		Context.Builder contextBuilder = Context.newBuilder("wasm").option("wasm.Builtins", "wasi_snapshot_preview1");
		Source.Builder sourceBuilder = Source.newBuilder("wasm", ByteSequence.create(binary), "example");
		Source source = sourceBuilder.build();
		Context context = contextBuilder.build();

		context.eval(source);

		Value memory = context.getBindings("wasm").getMember("main").getMember("memory");
		int stringLocation = 0;
		String s = "!gnirts tset a si sihT";
		int stringLength = writeStringToMemory(memory, s, stringLocation);
		Value function = context.getBindings("wasm").getMember("main").getMember("reverse");
		function.execute(stringLocation, stringLength);
		s = readStringFromMemory(memory, stringLocation, stringLength);
		System.out.println(s);
	}

	private static int writeStringToMemory(Value memory, String s, int location) {
		byte[] stringBytes = s.getBytes();
		for (int i = 0; i < stringBytes.length; i++) {
			memory.writeBufferByte(location + i, stringBytes[i]);
		}
		return stringBytes.length;
	}

	private static String readStringFromMemory(Value memory, int location, int size) {
		byte[] stringBytes = new byte[size];
		for (int i = 0; i < stringBytes.length; i++) {
			stringBytes[i] = memory.readBufferByte(location + i);
		}
		return new String(stringBytes);
	}
}