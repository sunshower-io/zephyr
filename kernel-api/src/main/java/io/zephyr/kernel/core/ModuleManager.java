package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.KernelModuleEntry;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationStatusGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleStatusGroup;
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
