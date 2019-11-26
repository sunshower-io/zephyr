package io.zephyr.kernel.memento;

import java.nio.file.FileSystem;

public interface MementoProvider {
  Memento newMemento();
  Memento newMemento(String name);
  Memento newMemento(String name, FileSystem fileSystem) throws Exception;
  Memento newMemento(String prefix, String name, FileSystem fileSystem) throws Exception;
}
