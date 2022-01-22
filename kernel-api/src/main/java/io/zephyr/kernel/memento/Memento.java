package io.zephyr.kernel.memento;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import lombok.val;

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
    return new NoOpMementoProvider();
  }

  static Memento load(ClassLoader... loaders) {
    return loadProvider(loaders).newMemento();
  }
}

class NoOpMemento implements Memento {

  String name;
  Object value;
  Map<String, Object> values;
  List<NoOpMemento> children;

  public NoOpMemento(String name) {

    this.name = name;
  }

  public NoOpMemento() {
    this(null);
  }

  @Override
  public void write(String name, Object value) {
    if (values == null) {
      values = new HashMap<>();
    }
    values.put(name, value);
  }

  @Override
  public void write(String name, int item) {
    write(name, Integer.valueOf(item));
  }

  @Override
  public void write(String name, long item) {

    write(name, Long.valueOf(item));
  }

  @Override
  public void write(String name, String value) {
    if (values == null) {
      values = new HashMap<>();
    }
    values.put(name, value);
  }

  @Override
  public Memento child(String name) {
    if (children == null) {
      children = new ArrayList<>();
    }
    val result = new NoOpMemento(name);
    children.add(result);
    return result;
  }

  @Override
  public Memento childNamed(String name) {
    if (children == null) {
      return new NoOpMemento();
    }
    return children.stream().filter(t -> t.name.equals(name)).findFirst().get();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T read(String name, Class<T> value) {
    return (T) values.get(name);
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void flush() throws IOException {}

  @Override
  public void write(OutputStream outputStream) throws Exception {}

  @Override
  public void read(InputStream inputStream) throws Exception {}

  @Override
  public List<Memento> getChildren(String name) {
    return Collections.emptyList();
  }

  @Override
  public Path locate(String prefix, FileSystem fs) {
    return Paths.get(System.getProperty("user.dir")).resolve("noopmemento");
  }
}

class NoOpMementoProvider implements MementoProvider {

  @Override
  public Memento newMemento() {
    return new NoOpMemento();
  }

  @Override
  public Memento newMemento(String name) {
    return new NoOpMemento();
  }

  @Override
  public Memento newMemento(String name, FileSystem fileSystem) throws Exception {
    return new NoOpMemento();
  }

  @Override
  public Memento newMemento(String prefix, String name, FileSystem fileSystem) throws Exception {
    return new NoOpMemento();
  }
}
