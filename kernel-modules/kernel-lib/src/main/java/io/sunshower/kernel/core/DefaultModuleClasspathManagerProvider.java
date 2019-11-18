package io.sunshower.kernel.core;

import io.zephyr.kernel.core.ModuleClasspathManager;
import io.zephyr.kernel.core.ModuleClasspathManagerProvider;
import io.zephyr.kernel.dependencies.DependencyGraph;

public class DefaultModuleClasspathManagerProvider implements ModuleClasspathManagerProvider {
  @Override
  public ModuleClasspathManager create(DependencyGraph graph) {
    return new KernelModuleLoader(graph);
  }
}
