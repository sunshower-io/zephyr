package io.sunshower.kernel.shell;

import java.io.PrintWriter;
import java.io.Reader;

public interface ShellConsole {

  void flush();

  ShellConsole format(String format, Object... args);

  ShellConsole printf(String format, Object... args);

  Reader getReader();

  String readLine();

  String readLine(String format, Object... args);

  String readPassword();

  String readPassword(String fmt, Object... args);

  PrintWriter getPrintWriter();
}
