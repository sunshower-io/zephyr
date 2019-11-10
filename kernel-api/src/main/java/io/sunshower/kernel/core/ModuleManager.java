package io.sunshower.kernel.core;

import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;

public interface ModuleManager extends KernelMember {

  ModuleInstallationStatusGroup prepare(ModuleInstallationGroup group);

  DependencyGraph getDependencyGraph();
}
