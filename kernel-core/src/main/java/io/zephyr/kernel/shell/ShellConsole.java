package io.zephyr.kernel.shell;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.ResourceBundle;

public interface ShellConsole {

  // Reset

  // Regular Colors

  void flush();

  ShellConsole format(String format, Object... args);

  ShellConsole printf(String format, Object... args);

  Reader getReader();

  String readLine();

  String readLine(String format, Object... args);

  String readPassword();

  String readPassword(String fmt, Object... args);

  PrintWriter getPrintWriter();

  ShellConsole writeln(String bundle, Color[] codes, Object... args);

  ShellConsole writeln(ResourceBundle resourceBundle, String bundle, Color[] codes, Object... args);

  ShellConsole write(ResourceBundle resourceBundle, String bundle, Color[] codes, Object... args);
}
