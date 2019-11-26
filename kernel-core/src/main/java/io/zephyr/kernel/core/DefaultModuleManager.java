package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.events.EventSource;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationStatusGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleStatusGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import lombok.experimental.Delegate;
import lombok.val;

@SuppressWarnings({
  "PMD.FinalizeOverloaded",
  "PMD.UnusedPrivateMethod",
  "PMD.UnusedFormalParameter",
  "PMD.AvoidInstantiatingObjectsInLoops"
})
public class DefaultModuleManager implements ModuleManager, EventSource {


  static final Logger log = Logging.get(DefaultModuleManager.class, "KernelMember");

  @Delegate private Kernel kernel;
  final DependencyGraph dependencyGraph;

  @Inject
  public DefaultModuleManager(DependencyGraph graph) {
    this.dependencyGraph = graph;
  }


  @Override
  public Module getModule(Coordinate coordinate) {
    return dependencyGraph.get(coordinate);
  }

  @Override
  public ModuleInstallationStatusGroup prepare(ModuleInstallationGroup group) {
    check();
    return new DefaultModuleInstallationStatusGroup(group, kernel);
  }

  @Override
  public ModuleLifecycleStatusGroup prepare(ModuleLifecycleChangeGroup group) {
    check();
    return new DefaultModuleLifecycleStatusChangeGroup(kernel, this, group);
  }

  @Override
  public DependencyGraph getDependencyGraph() {
    return dependencyGraph;
  }

  @Override
  public ModuleClasspathManager getModuleLoader() {
    check();
    return kernel.getModuleClasspathManager();
  }

  @Override
  public List<Module> getModules() {
    val results = new ArrayList<Module>();
    for (val module : dependencyGraph) {
      results.add(module);
    }
    return results;
  }

  @Override
  public List<Module> getModules(Lifecycle.State resolved) {
    val results = new ArrayList<Module>();
    for (val module : dependencyGraph) {
      if (module.getLifecycle().getState() == resolved) {
        results.add(module);
      }
    }
    return results;
  }

  @Override
  public void initialize(Kernel kernel) {
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "member.modulemanager.initialize", new Object[] {this, kernel});
    }
    if (kernel == null) {
      throw new IllegalStateException("cannot initialize with null kernel");
    }
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "member.modulemanager.complete", new Object[] {this, kernel});
    }
    this.kernel = kernel;
  }

  @Override
  public void finalize(Kernel kernel) {}

  private void check() {
    if (kernel == null) {
      throw new IllegalStateException(
          "Error: module download manager has not been properly initialized");
    }
  }
}
