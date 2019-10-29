package io.sunshower.kernel.core;

import java.util.ServiceLoader;

public interface ModuleClasspath {
  ClassLoader getClassLoader();

  ModuleLoader getModuleLoader();

  <S> ServiceLoader<S> resolveServiceLoader(Class<S> type);
}
