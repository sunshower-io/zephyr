package io.sunshower.kernel.lifecycle;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.dependencies.DependencyGraph;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ModuleResolver {
  static final String DEFAULT_MODULE_NAME = "module.droplet";

  private final Module module;
  private final DependencyGraph dependencyGraph;

  public Module resolver() {
    return null;
  }
}
