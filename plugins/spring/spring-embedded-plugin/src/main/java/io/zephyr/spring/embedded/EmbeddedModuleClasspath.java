package io.zephyr.spring.embedded;

import io.zephyr.kernel.core.ModuleClasspath;
import io.zephyr.kernel.core.ModuleLoader;
import java.util.ServiceLoader;

public class EmbeddedModuleClasspath implements ModuleClasspath {
  final ModuleLoader moduleLoader;

  public EmbeddedModuleClasspath(final ModuleLoader moduleLoader) {
    this.moduleLoader = moduleLoader;
  }

  @Override
  public ClassLoader getClassLoader() {
    return ClassLoader.getSystemClassLoader();
  }

  @Override
  public ModuleLoader getModuleLoader() {
    return moduleLoader;
  }

  @Override
  public <S> ServiceLoader<S> resolveServiceLoader(Class<S> type) {
    return ServiceLoader.load(type, getClassLoader());
  }
}
