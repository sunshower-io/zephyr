package io.zephyr.kernel.memento;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Memento<T> {

  void write(String name, Object value);

  void write(String name, int item);

  void write(String name, long item);

  void write(String name, String value);

  <U> Memento<U> child(String name, Class<U> type);

  <T> T read(String name, Class<T> value);

  void setValue(Object value);

  void setValue(String value);

  Object getValue();

  void flush() throws IOException;

  void write(OutputStream outputStream) throws Exception;

  void read(InputStream inputStream) throws Exception;
}
