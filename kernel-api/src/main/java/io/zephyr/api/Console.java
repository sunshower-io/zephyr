package io.zephyr.api;

import java.io.InputStream;

public interface Console {

  void writeln(String line, Color[] colors, Object... args);

  void errorln(String line, Object... args);

  String[] read();

  InputStream getInputStream();
}
