package io.sunshower.kernel.shell;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Scanner;

@SuppressFBWarnings
public class DelegatingShellConsole implements ShellConsole {

  final Scanner scanner;

  public DelegatingShellConsole() {
    scanner = new Scanner(System.in);
  }

  @Override
  public void flush() {}

  @Override
  public ShellConsole format(String format, Object... args) {
    System.out.format(format, args);
    return this;
  }

  @Override
  public ShellConsole printf(String format, Object... args) {
    System.out.format(format, args);
    return this;
  }

  @Override
  public Reader getReader() {
    return new InputStreamReader(System.in);
  }

  @Override
  public String readLine() {
    return scanner.nextLine();
  }

  @Override
  public String readLine(String format, Object... args) {
    printf(format, args);
    return readLine();
  }

  @Override
  public String readPassword() {
    return readLine();
  }

  @Override
  public String readPassword(String fmt, Object... args) {
    return readLine(fmt, args);
  }

  @Override
  public PrintWriter getPrintWriter() {
    return new PrintWriter(System.out);
  }
}
