package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Component;
import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Scope;
import io.zephyr.api.ModuleEvents;
import io.zephyr.common.io.Files;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.DefaultModule;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.dependencies.CyclicDependencyException;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.dependencies.UnresolvedDependencyException;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.log.Logging;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings({
  "PMD.UnusedPrivateMethod",
  "PMD.UnusedFormalParameter",
  "PMD.DataflowAnomalyAnalysis"
})
public class WritePluginDescriptorPhase extends Task {
  static final Logger log = Logging.get(WritePluginDescriptorPhase.class);

  public WritePluginDescriptorPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    final Set<Module> installedPlugins =
        scope.get(ModuleInstallationCompletionPhase.INSTALLED_PLUGINS);
    val kernel = scope.<SunshowerKernel>get("SunshowerKernel");

    handleKernelModules(scope, kernel);
    if (installedPlugins == null || installedPlugins.isEmpty()) {
      return null;
    }

    log.log(Level.INFO, "plugin.phase.writingplugins", installedPlugins.size());

    val moduleManager = kernel.getModuleManager();

    log.log(Level.INFO, "plugin.phase.resolvingplugins");

    val dependencyGraph = moduleManager.getDependencyGraph();

    checkForUnresolvedDependencies(kernel, dependencyGraph, installedPlugins);
    checkForCyclicDependencies(kernel, dependencyGraph, installedPlugins);

    saveAll(kernel, installedPlugins);
    resolvePlugins(kernel, moduleManager, installedPlugins);
    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_SET_INSTALLATION_COMPLETED, Events.create(installedPlugins));

    return null;
  }

  private void handleKernelModules(Scope scope, SunshowerKernel kernel) {
    log.log(Level.INFO, "plugin.phase.noplugins");
    final Set<Module> installedKernelModules =
        scope.get(ModuleInstallationCompletionPhase.INSTALLED_KERNEL_MODULES);
    if (installedKernelModules != null) {
      for (val module : installedKernelModules) {
        kernel.dispatchEvent(ModuleEvents.INSTALLED, Events.create(module));
      }
    }

    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_SET_INSTALLATION_COMPLETED,
        Events.create(scope.get(ModuleInstallationCompletionPhase.INSTALLED_KERNEL_MODULES)));
  }

  private void resolvePlugins(
      Kernel kernel, ModuleManager moduleManager, Set<Module> installedPlugins) {
    for (val module : installedPlugins) {
      val defaultModule = (DefaultModule) module;
      moduleManager.getModuleLoader().install(defaultModule);
      module.getLifecycle().setState(Lifecycle.State.Resolved);
      kernel.dispatchEvent(ModuleEvents.INSTALLED, Events.create(module));
    }
  }

  private void checkForCyclicDependencies(
      Kernel kernel, DependencyGraph dependencyGraph, Set<Module> installedPlugins) {
    val prospective = dependencyGraph.clone();
    prospective.addAll(installedPlugins);
    val partition = prospective.computeCycles();
    if (partition.isCyclic()) {
      val ex = new CyclicDependencyException();
      for (val cycle : partition.getElements()) {
        if (cycle.isCyclic()) {
          ex.addComponent(cycle);
          updateComponents(kernel, installedPlugins, ex, cycle);
        }
      }

      throw ex;
    }
    log.info("plugin.phase.nocycles");

    dependencyGraph.addAll(installedPlugins);
  }

  private void updateComponents(
      Kernel kernel,
      Set<Module> installedPlugins,
      CyclicDependencyException ex,
      Component<DirectedGraph.Edge<Coordinate>, Coordinate> cycle) {
    for (val el : cycle.getElements()) {
      val coord = el.getSnd();
      for (val plugin : installedPlugins) {
        handleCycle(kernel, ex, coord, plugin, cycle);
      }
    }
  }

  private void handleCycle(
      Kernel kernel,
      CyclicDependencyException ex,
      Coordinate coord,
      Module plugin,
      Component<DirectedGraph.Edge<Coordinate>, Coordinate> cycle) {
    if (coord.equals(plugin.getCoordinate())) {
      fireCyclicDependencyFailure(kernel, coord, cycle);
      checkClose(ex, plugin);
    }
  }

  private void fireCyclicDependencyFailure(
      Kernel kernel,
      Coordinate coord,
      Component<DirectedGraph.Edge<Coordinate>, Coordinate> cycle) {
    kernel.dispatchEvent(
        ModuleEvents.INSTALL_FAILED,
        Events.create(new DependencyGraph.CyclicDependencySet(coord, cycle)));
  }

  private void checkClose(CyclicDependencyException ex, Module plugin) {
    val fs = plugin.getFileSystem();
    if (fs != null) {
      try {
        fs.close();
      } catch (Exception x) {
        ex.addSuppressed(x);
      }
    }
  }

  private void checkForUnresolvedDependencies(
      Kernel kernel, DependencyGraph dependencyGraph, Collection<Module> installedPlugins) {
    val results = dependencyGraph.getUnresolvedDependencies(installedPlugins);
    val unsatisfied = new HashSet<DependencyGraph.UnsatisfiedDependencySet>();
    for (val unresolvedDependency : results) {

      if (!unresolvedDependency.isSatisfied()) {
        val plugin = unresolvedDependency.getSource();
        for (val installedPlugin : installedPlugins) {
          if (plugin.equals(installedPlugin.getCoordinate())) {
            try {
              val fs = installedPlugin.getFileSystem();
              if (fs != null) {
                fs.close();
              }
            } catch (Exception ex) {
              log.log(Level.WARNING, "failed to close plugin filesystem {0}", plugin);
            }
          }
        }
        unsatisfied.add(unresolvedDependency);
      }
    }

    if (!unsatisfied.isEmpty()) {
      fireUnresolvedDependencies(kernel, unsatisfied);
      log.warning("plugin.phase.unresolveddependencies");
      throw new UnresolvedDependencyException("unresolved dependencies detected", unsatisfied);
    }
  }

  private void fireUnresolvedDependencies(
      Kernel kernel, Set<DependencyGraph.UnsatisfiedDependencySet> unsatisfied) {
    for (val unsatisfiedDependencySet : unsatisfied) {
      kernel.dispatchEvent(ModuleEvents.INSTALL_FAILED, Events.create(unsatisfiedDependencySet));
    }
  }

  private void saveAll(SunshowerKernel kernel, Set<Module> installedPlugins) {
    for (val plugin : installedPlugins) {
      val pfs = plugin.getFileSystem();
      try {
        val pmemento = plugin.save();
        Files.tryWrite(pmemento.locate("plugin", pfs), pmemento);

      } catch (Exception e) {
        log.log(Level.WARNING, "failed to write descriptor", e);
      }
    }
  }
}
