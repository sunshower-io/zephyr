package io.sunshower.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.ModuleManager;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.dependencies.UnresolvedDependencyException;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.memento.core.PluginCaretaker;
import lombok.val;

import java.nio.file.FileSystem;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WritePluginDescriptorPhase extends Task {
  static final Logger log = Logging.get(WritePluginDescriptorPhase.class);

  public WritePluginDescriptorPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    final List<Module> installedPlugins =
        scope.get(ModuleInstallationCompletionPhase.INSTALLED_PLUGINS);

    if (installedPlugins == null || installedPlugins.isEmpty()) {
      log.log(Level.INFO, "plugin.phase.noplugins");
      return null;
    }

    log.log(Level.INFO, "plugin.phase.writingplugins", installedPlugins.size());

    val kernel = scope.<SunshowerKernel>get("SunshowerKernel");
    ServiceLoader<PluginCaretaker> caretakers =
        ServiceLoader.load(PluginCaretaker.class, kernel.getClassLoader());

    val caretaker = caretakers.findFirst();
    if (caretaker.isEmpty()) {
      log.log(Level.WARNING, "plugin.phase.nocaretakers");
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }

    val actualCaretaker = caretaker.get();
    val moduleManager = kernel.getModuleManager();

    saveAll(kernel.getFileSystem(), moduleManager, actualCaretaker, installedPlugins);

    log.log(Level.INFO, "plugin.phase.resolvingplugins");

    val dependencyGraph = moduleManager.getDependencyGraph();

    checkForUnresolvedDependencies(dependencyGraph, installedPlugins);
    checkForCyclicDependencies(dependencyGraph, installedPlugins);

    return null;
  }

  private void checkForCyclicDependencies(
      DependencyGraph dependencyGraph, List<Module> installedPlugins) {
    val prospective = dependencyGraph.clone();
    val partition = prospective.computeCycles();
    if(partition.isCyclic()) {
      val ex = new CyclicDependencyException();

    }


  }

  private void checkForUnresolvedDependencies(
      DependencyGraph dependencyGraph, Collection<Module> installedPlugins) {
    val results = dependencyGraph.getUnresolvedDependencies(installedPlugins);
    UnresolvedDependencyException ex = null; // exceptions are expensive to create
    for (val unresolvedDependency : results) {
      if (!unresolvedDependency.isSatisfied()) {
        if (ex == null) {
          ex = new UnresolvedDependencyException();
        }
        ex.add(unresolvedDependency);
      }
    }
    if (ex != null) {
      throw ex;
    }
  }

  private void saveAll(
      FileSystem fileSystem,
      ModuleManager moduleManager,
      PluginCaretaker actualCaretaker,
      List<Module> installedPlugins) {}
}
