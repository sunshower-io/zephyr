package io.zephyr.kernel.core;

import io.zephyr.kernel.Source;
import java.io.File;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor
public class ModuleSource implements Source {
  @Getter final URI location;

  @Override
  @SneakyThrows
  public boolean is(File file) {
    return location.toURL().getFile().equals(file.getAbsolutePath());
  }
}
