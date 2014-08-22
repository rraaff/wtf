package wtfplugin.console;

import java.io.IOException;
import java.io.Writer;

public class ConsoleWriter extends Writer {

	@Override
	public void close() throws IOException {

	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		MysqlConsoleFactory.log(new String(cbuf, off, len));
	}

}
