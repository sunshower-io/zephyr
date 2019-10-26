package io.sunshower.kernel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;

public class Assembly {

  @Getter private final File file;
  @Getter private Set<String> subpaths;

  public Assembly(final File file) {
    this.file = file;
    this.subpaths = new HashSet<>();
  }

  public void addSubpath(@NonNull String subpath) {
    subpaths.add(subpath);
  }
}
