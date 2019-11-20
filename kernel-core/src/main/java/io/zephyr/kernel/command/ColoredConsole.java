package io.zephyr.kernel.command;

import io.zephyr.api.Color;
import io.zephyr.api.Console;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.regex.Pattern;
import lombok.val;

public class ColoredConsole implements Console {

  static final Pattern pattern = Pattern.compile("\\s+");

  private final InputStream in;
  private final PrintStream out;
  private final Scanner scanner;

  public ColoredConsole(InputStream in, PrintStream out) {
    this.in = in;
    this.out = out;
    this.scanner = new Scanner(in, StandardCharsets.UTF_8);
  }

  public ColoredConsole() {
    this(System.in, System.out);
  }

  @Override
  public void writeln(String line, Color[] colors, Object... args) {
    val sbuilder = new StringBuilder();
    for (val color : colors) {
      sbuilder.append(color);
    }
    sbuilder.append(MessageFormat.format(line, args));
    sbuilder.append(Color.Reset);
    out.println(sbuilder.toString());
  }

  @Override
  public void errorln(String line, Object... args) {
    writeln(line, Color.colors(Color.Red), args);
  }

  @Override
  @SuppressFBWarnings
  public String[] read() {
    return pattern.split(scanner.nextLine());
  }

  @Override
  public InputStream getInputStream() {
    return in;
  }

  @Override
  public void successln(String string, Object... args) {
    writeln(string, Color.colors(Color.GreenBoldBright), args);
  }

  @Override
  public PrintStream getWriter() {
    return out;
  }
}