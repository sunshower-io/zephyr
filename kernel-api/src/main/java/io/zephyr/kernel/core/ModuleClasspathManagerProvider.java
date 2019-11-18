package io.zephyr.kernel.core;

import io.zephyr.kernel.dependencies.DependencyGraph;

public interface ModuleClasspathManagerProvider {

  ModuleClasspathManager create(DependencyGraph graph);
}
