package io.sunshower.kernel;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.NonNull;
import lombok.val;

public interface Module extends Comparable<Module> {

  enum Type {
    Plugin,
    KernelModule;

    public static Type parse(@NonNull String value) {
      val normalized = value.trim().toLowerCase();
      switch (normalized) {
        case "plugin":
          return Type.Plugin;
        case "kernel-module":
        case "kernelmodule":
          return Type.KernelModule;
      }
      throw new IllegalArgumentException(
          "value '"
              + value
              + "' is not a valid type.  Must be ['kernel-module', 'kernelModule', 'plugin'] (case-insensitive)");
    }
  }

  int getOrder();

  /** @return the relative paths of any created library directories */
  Set<Library> getLibraries();

  Type getType();

  Path getModuleDirectory();

  Assembly getAssembly();

  Source getSource();

  Lifecycle getLifecycle();

  Coordinate getCoordinate();

  FileSystem getFileSystem();

  ClassLoader getClassLoader();

  Set<Dependency> getDependencies();

  default boolean dependsOn(Module m, Transitivity transitivity) {
    return dependsOn(m.getCoordinate(), transitivity);
  }

  boolean dependsOn(Coordinate m, Transitivity transitivity);

  <S> ServiceLoader<S> resolveServiceLoader(Class<S> type);

  default int compareTo(Module m) {
    return getCoordinate().compareTo(m.getCoordinate());
  }
}
