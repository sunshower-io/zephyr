package io.sunshower.kernel.core;

import io.sunshower.kernel.*;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.dependencies.ModuleCycleDetector;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.status.Status;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.val;

public class DefaultModuleManager implements ModuleManager {

  private static final Logger log = Logging.get(DefaultModuleManager.class);

  private final DefaultModuleContext context;
  private final Object moduleLoaderLock = new Object();
  private final ModuleClasspathManager classpathManager;

  /** these must be in lock-step, protected by <i>DefaultModuleManager.lock</i> */
  private final DependencyGraph dependencyGraph;

  private final Map<Module.Type, List<Module>> modules;

  @Inject
  public DefaultModuleManager(
      @NonNull final ModuleContext context,
      @NonNull final ModuleClasspathManager classpathManager,
      @NonNull final DependencyGraph graph) {
    this.context = (DefaultModuleContext) context;
    this.dependencyGraph = graph;
    this.classpathManager = classpathManager;
    modules = new EnumMap<>(Module.Type.class);
  }

  @Override
  public void addStatus(Status status) {}

  @Override
  public List<Module> getModules(Module.Type type) {
    return modules.computeIfAbsent(type, DefaultModuleManager::newList);
  }

  @Override
  public void resolve(Module module) {
    val lifecycle = module.getLifecycle();
    val coordinate = module.getCoordinate();
    if (lifecycle.getState().isAtLeast(Lifecycle.State.Resolved)) {
      log.log(Level.INFO, "module.state.resolved.already", coordinate);
      return;
    }
    log.log(Level.INFO, "module.state.attempting.resolve", coordinate);

    checkDependencies(module);
    classpathManager.install(module);

    val mod = ((DefaultModule) module).getModuleClasspath();
    if (mod == null) {
      throw new IllegalStateException(
          "Something weird happened--kernel didn't install module correctly or it's been unloaded or something");
    }

    lifecycle.setState(Lifecycle.State.Resolved);
    context.dispatch(new ModuleLifecycleEvent(module, Lifecycle.State.Resolved));
  }

  @Override
  public LifecycleAction prepareFor(Lifecycle.State starting, Module dependent) {
    switch (starting) {
      case Starting:
        return new StartLifecycleAction(
            dependent, starting, VisitingActionTree.createFrom(dependent, dependencyGraph));
      case Stopping:
        return null;
      case Active:
        return null;
      default:
        return null;
        //        return new VisitingActionTree(this, new StartAction(), dependent);
    }
  }

  @Override
  public void install(Module module) {
    val lifecycle = module.getLifecycle();
    if (isAtLeast(lifecycle, Lifecycle.State.Installed)) {
      log.log(Level.INFO, "module.lifecycle.state.atleast");
      return;
    }
    synchronized (moduleLoaderLock) {
      lifecycle.setState(Lifecycle.State.Installed);
      dependencyGraph.add(module);
      modules.computeIfAbsent(module.getType(), DefaultModuleManager::newList).add(module);
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void checkDependencies(Module module) {
    val current = dependencyGraph;
    val unresolved = current.getUnresolvedDependencies(module);
    if (!unresolved.isEmpty()) {
      log.log(Level.WARNING, "module.dependency.unsatisfied", unresolved);
      module.getLifecycle().setState(Lifecycle.State.Failed);
      throw new UnsatisfiedDependencyException(module, unresolved);
    }

    val components = ModuleCycleDetector.newDetector(current).compute();

    if (components.hasCycle()) {
      module.getLifecycle().setState(Lifecycle.State.Failed);
      throw new CyclicModuleDependencyException(module, components);
    }
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private boolean isAtLeast(Lifecycle lifecycle, Lifecycle.State installed) {
    val state = lifecycle.getState();
    if (state == null) {
      return false;
    }
    return state.isAtLeast(installed);
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private static <K, T> List<T> newList(K a) {
    return new ArrayList<>();
  }
}
