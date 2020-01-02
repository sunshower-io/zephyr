package io.zephyr.kernel.core;

import io.zephyr.kernel.dependencies.DependencyGraph;

public class DefaultModuleClasspathManagerProvider implements ModuleClasspathManagerProvider {
  @Override
  public ModuleClasspathManager create(DependencyGraph graph, Kernel kernel) {
    return new KernelModuleLoader(graph, kernel);
  }
}
