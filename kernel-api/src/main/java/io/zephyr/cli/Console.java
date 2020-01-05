package io.zephyr.cli;

import java.io.InputStream;
import java.io.PrintStream;
import lombok.val;

public interface Console {
  default void flush() {}

  default Object getTarget() {
    return getWriter();
  }

  default String writeString(String line, Color[] colors, Object[] args) {
    val sbuilder = new StringBuilder();
    for (val color : colors) {
      sbuilder.append(color);
    }
    sbuilder.append(String.format(line, args));
    sbuilder.append(Color.Reset);
    return sbuilder.toString();
  }

  void writeln(String line, Color[] colors, Object... args);

  void errorln(String line, Object... args);

  String[] read();

  InputStream getInputStream();

  void successln(String line, Object... args);

  PrintStream getWriter();

  void write(Object o);
}
