package io.sunshower.yaml.state;

import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.memento.MementoProvider;
import lombok.val;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class YamlMementoProvider implements MementoProvider {
  @Override
  public Memento newMemento() {
    return new YamlMemento();
  }

  @Override
  public Memento newMemento(String name) {
    return new YamlMemento(name);
  }

  @Override
  public Memento newMemento(String name, FileSystem fileSystem) throws Exception {
    return newMemento(name, name, fileSystem);
  }

  @Override
  public Memento newMemento(String prefix, String name, FileSystem fileSystem) throws Exception {
    val result = new YamlMemento(name);
    try (val inputStream =
        Files.newInputStream(result.locate(prefix, fileSystem), StandardOpenOption.READ)) {
      result.read(inputStream);
    }
    return result;
  }
}
