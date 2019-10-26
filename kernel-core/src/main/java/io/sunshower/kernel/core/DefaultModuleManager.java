package io.sunshower.kernel.core;

import static java.util.Collections.singleton;

import io.sunshower.common.Collections;
import io.sunshower.kernel.DefaultModule;
import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.actions.StartLifecycleAction;
import io.sunshower.kernel.core.actions.VisitingActionTree;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.dependencies.ModuleCycleDetector;
import io.sunshower.kernel.dependencies.UnsatisfiedDependencyException;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.status.Status;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.val;

public class DefaultModuleManager implements ModuleManager {

  static final Logger log = Logging.get(DefaultModuleManager.class);

  private final DefaultModuleContext context;
  private final Object moduleLoaderLock = new Object();

  /** these must be in lock-step, protected by <i>DefaultModuleManager.lock</i> */
  @SuppressWarnings("PMD.AvoidUsingVolatile")
  private volatile DependencyGraph dependencyGraph;

  @SuppressWarnings("PMD.AvoidUsingVolatile")
  private volatile KernelModuleLoader moduleLoader;

  private final Map<Module.Type, List<Module>> modules;

  @Inject
  public DefaultModuleManager(@NonNull DefaultModuleContext context) {
    this.context = context;
    modules = new EnumMap<>(Module.Type.class);
  }

  @Override
  public void addStatus(Status status) {}

  @Override
  public List<Module> getModules(Module.Type type) {
    return modules.computeIfAbsent(type, Collections::newList);
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
    moduleLoader.install(module);

    val mod = ((DefaultModule) module).getModule();
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
      modules.computeIfAbsent(module.getType(), Collections::newList).add(module);
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void checkDependencies(Module module) {
    DependencyGraph prospective;
    try {
      val current = getDependencyGraph(module);
      prospective = current.add(module);
    } catch (UnsatisfiedDependencyException ex) {
      log.log(Level.WARNING, "module.dependency.unsatisfied", ex.getMessage());
      module.getLifecycle().setState(Lifecycle.State.Failed);
      throw ex;
    }

    val components = ModuleCycleDetector.newDetector(prospective).compute();

    if (components.hasCycle()) {
      throw new CyclicModuleDependencyException(module, components);
    }
    synchronized (moduleLoaderLock) {
      dependencyGraph = prospective;
      moduleLoader.updateDependencies(dependencyGraph);
    }
  }

  private DependencyGraph getDependencyGraph(Module module) {
    DependencyGraph local = dependencyGraph;
    if (local == null) {
      synchronized (moduleLoaderLock) {
        local = dependencyGraph;
        if (local == null) {
          dependencyGraph = local = DependencyGraph.create(singleton(module));
          moduleLoader = new KernelModuleLoader(dependencyGraph);
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
}
