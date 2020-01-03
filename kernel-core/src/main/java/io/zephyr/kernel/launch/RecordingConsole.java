package io.zephyr.kernel.launch;

import io.zephyr.cli.Color;
import io.zephyr.cli.Console;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Synchronized;
import lombok.val;

public class RecordingConsole implements Console, Serializable {
  private static final long serialVersionUID = 1L;

  private final List<String> messages;

  public RecordingConsole() {
    messages = new ArrayList<>();
  }

  public List<String> getMessages() {
    return messages;
  }

  @Override
  @Synchronized
  public void writeln(String line, Color[] colors, Object... args) {
    val sbuilder = writeString(line, colors, args);
    messages.add(sbuilder);
  }

  @Override
  @Synchronized
  public void errorln(String line, Object... args) {
    writeln(line, Color.colors(Color.RedBright), args);
  }

  @Override
  @Synchronized
  public String[] read() {
    return new String[0];
  }

  @Override
  @Synchronized
  public InputStream getInputStream() {
    return new OutputArrayInputStream(messages);
  }

  @Override
  @Synchronized
  public void successln(String line, Object... args) {
    writeln(line, Color.colors(Color.Green), args);
  }

  @Override
  @Synchronized
  public PrintStream getWriter() {
    throw new UnsupportedOperationException("No writer!");
  }

  @Override
  @Synchronized
  public void write(Object o) {
    messages.add(String.valueOf(o));
  }

  @Override
  @Synchronized
  public List<String> getTarget() {
    return getMessages();
  }

  @Override
  @Synchronized
  public void flush() {
    messages.clear();
  }
}
