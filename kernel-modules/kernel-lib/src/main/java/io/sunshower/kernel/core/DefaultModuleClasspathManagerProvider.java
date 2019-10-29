package io.sunshower.kernel.core;

import io.sunshower.kernel.dependencies.DependencyGraph;

public class DefaultModuleClasspathManagerProvider implements ModuleClasspathManagerProvider {
  @Override
  public ModuleClasspathManager create(DependencyGraph graph) {
    return new KernelModuleLoader(graph);
  }
}
