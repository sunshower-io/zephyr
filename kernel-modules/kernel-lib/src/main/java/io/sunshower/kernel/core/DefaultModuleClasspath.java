package io.sunshower.kernel.core;

import java.util.ServiceLoader;
import org.jboss.modules.Module;

public class DefaultModuleClasspath implements ModuleClasspath {
  final Module module;
  final ModuleLoader moduleLoader;

  public DefaultModuleClasspath(Module module, ModuleLoader loader) {
    this.module = module;
    this.moduleLoader = loader;
  }

  @Override
  public ClassLoader getClassLoader() {
    return module.getClassLoader();
  }

  @Override
  public ModuleLoader getModuleLoader() {
    return moduleLoader;
  }

  @Override
  public <S> ServiceLoader<S> resolveServiceLoader(Class<S> type) {
    return module.loadService(type);
  }
}
