package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;
import io.sunshower.kernel.module.ModuleLifecycleChangeGroup;
import io.sunshower.kernel.module.ModuleLifecycleStatusGroup;
import java.util.List;

public interface ModuleManager extends KernelMember {

  Module getModule(Coordinate coordinate);

  ModuleInstallationStatusGroup prepare(ModuleInstallationGroup group);

  ModuleLifecycleStatusGroup prepare(ModuleLifecycleChangeGroup group);

  DependencyGraph getDependencyGraph();

  ModuleClasspathManager getModuleLoader();

  List<Module> getModules();

  List<Module> getModules(Lifecycle.State resolved);
}
