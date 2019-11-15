package io.sunshower.kernel.shell;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Scanner;
import lombok.val;

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

  @Override
  public ShellConsole writeln(String bundle, Color[] codes, Object... args) {
    val builder = new StringBuilder();
    for (val color : codes) {
      builder.append(color);
    }
    builder.append(String.format(bundle, args));
    builder.append(Color.Reset);
    System.out.println(builder);
    return this;
  }

  @Override
  public ShellConsole writeln(
      ResourceBundle resourceBundle, String bundle, Color[] codes, Object... args) {
    write(resourceBundle, bundle, codes, args);
    System.out.println();
    return this;
  }

  @Override
  public ShellConsole write(
      ResourceBundle resourceBundle, String bundle, Color[] codes, Object... args) {
    val string = resourceBundle.getString("bundle");
    val message = MessageFormat.format(string, args);
    val builder = new StringBuilder();
    for (val color : codes) {
      builder.append(color);
    }
    builder.append(message);
    builder.append(Color.Reset);
    System.out.print(builder);
    return this;
  }
}
