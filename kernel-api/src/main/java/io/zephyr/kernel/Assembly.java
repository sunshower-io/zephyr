package io.zephyr.kernel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;

public class Assembly {

  @Getter private final File file;
  @Getter private final Set<String> subpaths;

  @Getter private final Set<Library> libraries;

  public Assembly(final File file) {
    this.file = file;
    this.subpaths = new HashSet<>();
    this.libraries = new HashSet<>();
  }

  public void addLibrary(@NonNull Library library) {
    libraries.add(library);
  }

  public void addSubpath(@NonNull String subpath) {
    subpaths.add(subpath);
  }
}
