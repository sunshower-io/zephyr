package io.sunshower.kernel.core;

import io.sunshower.kernel.dependencies.DependencyGraph;

public interface ModuleClasspathManagerProvider {

  ModuleClasspathManager create(DependencyGraph graph);
}
