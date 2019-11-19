package io.zephyr.kernel.command;

import io.zephyr.api.Color;
import io.zephyr.api.Console;
import lombok.val;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ColoredConsole implements Console {

  static final Pattern pattern = Pattern.compile("\\s+");

  private final InputStream in;
  private final PrintStream out;
  private final Scanner scanner;

  public ColoredConsole(InputStream in, PrintStream out) {
    this.in = in;
    this.out = out;
    this.scanner = new Scanner(in);
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
    System.out.println(sbuilder.toString());
  }

  @Override
  public void errorln(String line, Object... args) {
    writeln(line, Color.colors(Color.Red), args);
  }

  @Override
  public String[] read() {
    return pattern.split(scanner.nextLine());
  }

  @Override
  public InputStream getInputStream() {
    return in;
  }
}
