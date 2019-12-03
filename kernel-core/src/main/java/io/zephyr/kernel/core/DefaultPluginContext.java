package io.zephyr.kernel.core;

import io.zephyr.PluginContext;
import io.zephyr.kernel.Module;
import java.util.List;
import java.util.function.Predicate;

public class DefaultPluginContext implements PluginContext {
  final Module module;
  final Kernel kernel;

  public DefaultPluginContext(final Module module, final Kernel kernel) {
    this.module = module;
    this.kernel = kernel;
  }

  @Override
  public Module getModule() {
    return module;
  }

  @Override
  public List<Module> getModules(Predicate<Module> filter) {
    return kernel.getModuleManager().getModules();
  }
}
