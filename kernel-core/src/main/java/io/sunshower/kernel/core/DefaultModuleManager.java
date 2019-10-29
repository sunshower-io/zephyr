package io.sunshower.kernel.core;

import static java.util.Collections.singleton;

import io.sunshower.kernel.*;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.dependencies.DefaultDependencyGraph;
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
  @SuppressWarnings("PMD.AvoidUsingVolatile")
  private volatile DependencyGraph defaultDependencyGraph;

  private final Map<Module.Type, List<Module>> modules;

  @Inject
  public DefaultModuleManager(
      @NonNull final DefaultModuleContext context,
      @NonNull final ModuleClasspathManager classpathManager) {
    this.context = context;
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
        //        return new StartLifecycleAction(
        //            dependent, starting, VisitingActionTree.createFrom(dependent,
        // defaultDependencyGraph));
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
      modules.computeIfAbsent(module.getType(), DefaultModuleManager::newList).add(module);
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void checkDependencies(Module module) {
    val current = getDependencyGraph(module);
    try {
      current.add(module);
    } catch (UnsatisfiedDependencyException ex) {
      log.log(Level.WARNING, "module.dependency.unsatisfied", ex.getMessage());
      module.getLifecycle().setState(Lifecycle.State.Failed);
      throw ex;
    }

    val components = ModuleCycleDetector.newDetector(current).compute();

    if (components.hasCycle()) {
      throw new CyclicModuleDependencyException(module, components);
    }
  }

  private DependencyGraph getDependencyGraph(Module module) {
    DependencyGraph local = defaultDependencyGraph;
    if (local == null) {
      synchronized (moduleLoaderLock) {
        local = defaultDependencyGraph;
        if (local == null) {
          defaultDependencyGraph = local = DefaultDependencyGraph.create(singleton(module));
        }
      }
    }
    return local;
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
