package io.zephyr.kernel.core;

import io.zephyr.api.*;
import io.zephyr.kernel.AsynchronousModuleThreadTracker;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.ModuleThread;
import java.util.List;
import java.util.function.Predicate;

public class DefaultPluginContext implements ModuleContext {

  final Module module;
  final Kernel kernel;

  public DefaultPluginContext(final Module module, final Kernel kernel) {
    this.module = module;
    this.kernel = kernel;
  }

  @Override
  public <T> RequirementRegistration<T> createRequirement(Requirement<T> requirement) {
    return null;
  }

  @Override
  public <T> Predicate<T> createFilter(Query<T> query) {
    return null;
  }

  @Override
  public <T> CapabilityRegistration<T> provide(CapabilityDefinition<T> capability) {
    return null;
  }

  @Override
  public Module getModule() {
    return module;
  }

  @Override
  public List<Module> getModules(Predicate<Module> filter) {
    return kernel.getModuleManager().getModules();
  }

  @Override
  public ModuleTracker createModuleTracker(Predicate<Module> filter) {
    return new AsynchronousModuleThreadTracker(
        kernel, module, (ModuleThread) module.getTaskQueue(), filter);
  }
}
