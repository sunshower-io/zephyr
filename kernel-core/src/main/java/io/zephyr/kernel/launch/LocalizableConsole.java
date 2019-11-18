package io.zephyr.kernel.launch;

import io.zephyr.kernel.shell.Color;
import io.zephyr.kernel.shell.ShellConsole;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ResourceBundle;

public class LocalizableConsole implements ShellConsole, AutoCloseable {
  private final ShellConsole delegate;
  private final ResourceBundle resourceBundle;

  public LocalizableConsole(ShellConsole console, Class<?> type) {
    delegate = console;
    this.resourceBundle = ResourceBundle.getBundle("i18n." + type.getName());
  }

  @Override
  public void flush() {
    delegate.flush();
  }

  @Override
  public ShellConsole format(String format, Object... args) {
    return delegate.format(format, args);
  }

  @Override
  public ShellConsole printf(String format, Object... args) {
    return null;
  }

  @Override
  public Reader getReader() {
    return null;
  }

  @Override
  public String readLine() {
    return null;
  }

  @Override
  public String readLine(String format, Object... args) {
    return null;
  }

  @Override
  public String readPassword() {
    return null;
  }

  @Override
  public String readPassword(String fmt, Object... args) {
    return null;
  }

  @Override
  public PrintWriter getPrintWriter() {
    return null;
  }

  @Override
  public ShellConsole writeln(String bundle, Color[] codes, Object... args) {
    return writeln(resourceBundle, bundle, codes, args);
  }

  @Override
  public ShellConsole writeln(
      ResourceBundle resourceBundle, String bundle, Color[] codes, Object... args) {
    return null;
  }

  @Override
  public ShellConsole write(
      ResourceBundle resourceBundle, String bundle, Color[] codes, Object... args) {
    return null;
  }

  @Override
  public void close() throws Exception {}
}
