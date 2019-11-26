package io.zephyr.kernel.memento;

import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

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

  Path locate(String prefix, FileSystem fs);

  static MementoProvider loadProvider(ClassLoader... loaders) {
    for (val classloader : loaders) {
      var loader = ServiceLoader.load(MementoProvider.class, classloader).iterator();
      if (loader.hasNext()) {
        return loader.next();
      }
    }
    throw new NoSuchElementException(
        "No Memento service loader defined in any specified classloader");
  }

  static Memento load(ClassLoader... loaders) {
    return loadProvider(loaders).newMemento();
  }
}
