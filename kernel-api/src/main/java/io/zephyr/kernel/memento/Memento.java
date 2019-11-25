package io.zephyr.kernel.memento;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface Memento {

  void write(String name, Object value);

  void write(String name, int item);

  void write(String name, long item);

  void write(String name, String value);

  Memento child(String name);

  Memento childNamed(String name);

  <T> T read(String name, Class<T> value);

  void setValue(Object value);

  void setValue(String value);

  Object getValue();

  void flush() throws IOException;

  void write(OutputStream outputStream) throws Exception;

  void read(InputStream inputStream) throws Exception;

  List<Memento> getChildren(String name);
}
