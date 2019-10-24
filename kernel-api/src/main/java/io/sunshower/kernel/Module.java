package io.sunshower.kernel;

import java.nio.file.FileSystem;
import java.util.ServiceLoader;
import java.util.Set;

public interface Module {

  enum Type {
    Plugin,
    KernelModule
  }

  Type getType();

  LifeCycle getLifecycle();

  Coordinate getCoordinate();

  FileSystem getFileSystem();

  ClassLoader getClassLoader();

  Set<Coordinate> getDependencies();

  default boolean dependsOn(Module m, Transitivity transitivity) {
    return dependsOn(m.getCoordinate(), transitivity);
  }

  boolean dependsOn(Coordinate m, Transitivity transitivity);

  <S> ServiceLoader<S> resolveServiceLoader(Class<S> type);
}
