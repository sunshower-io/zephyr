package io.zephyr.kernel.memento;

import java.io.InputStream;
import java.io.OutputStream;

public interface Memento<T> {
  void read(InputStream inputStream);

  void write(OutputStream outputStream);
}
