package io.zephyr.spring.embedded;

import io.zephyr.kernel.core.ModuleClasspath;
import io.zephyr.kernel.core.ModuleLoader;
import java.util.ServiceLoader;

public class EmbeddedModuleClasspath implements ModuleClasspath {
  final ModuleLoader moduleLoader;
  final ClassLoader delegateClassloader;

  public EmbeddedModuleClasspath(
      final ModuleLoader moduleLoader, final ClassLoader delegateClassloader) {
    this.moduleLoader = moduleLoader;
    this.delegateClassloader = delegateClassloader;
  }

  public EmbeddedModuleClasspath(final ModuleLoader loader) {
    this(loader, ClassLoader.getSystemClassLoader());
  }

  @Override
  public ClassLoader getClassLoader() {
    return delegateClassloader;
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
