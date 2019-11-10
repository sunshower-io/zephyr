package io.sunshower.kernel.core;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;

import java.util.List;

public interface ModuleManager extends KernelMember {

  ModuleInstallationStatusGroup prepare(ModuleInstallationGroup group);

  DependencyGraph getDependencyGraph();

  ModuleClasspathManager getModuleLoader();

  List<Module> getModules(Lifecycle.State resolved);
}
