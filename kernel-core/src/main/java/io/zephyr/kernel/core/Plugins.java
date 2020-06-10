package io.zephyr.kernel.core;

import io.sunshower.gyre.Component;
import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Pair;
import io.sunshower.gyre.Scope;
import io.zephyr.api.ModuleEvents;
import io.zephyr.common.io.Files;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.actions.ModuleInstallationCompletionPhase;
import io.zephyr.kernel.core.actions.ModulePhaseEvents;
import io.zephyr.kernel.core.actions.WritePluginDescriptorPhase;
import io.zephyr.kernel.dependencies.CyclicDependencyException;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.dependencies.UnresolvedDependencyException;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.log.Logging;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings({
  "PMD.UnusedPrivateMethod",
  "PMD.UnusedFormalParameter",
  "PMD.DataflowAnomalyAnalysis"
})
public class Plugins {

  static final String FILE_SYSTEM_URI_TEMPLATE = "droplet://%s.%s?version=%s";

  @SuppressWarnings({"PMD.CloseResource", "PMD.DataflowAnomalyAnalysis"})
  public static final Pair<String, FileSystem> locateFilesystem(
      Coordinate coordinate, Kernel kernel) throws IOException {
    val uri =
        String.format(
            FILE_SYSTEM_URI_TEMPLATE,
            coordinate.getGroup(),
            coordinate.getName(),
            coordinate.getVersion());
    FileSystem fs = FileSystems.getFileSystem(URI.create(uri));
    return Pair.of(uri, fs);
  }

  static final Object lock = new Object();
  /**
   * this method retrieves or creates the filesystem
   *
   * @param coordinate
   * @param kernel
   * @return
   * @throws IOException
   */
  @SuppressWarnings({
    "PMD.CloseResource",
    "PMD.DataflowAnomalyAnalysis",
    "PMD.AvoidSynchronizedAtMethodLevel"
  })
  public static Pair<String, FileSystem> getFileSystem(Coordinate coordinate, Kernel kernel)
      throws IOException {
    val uriValue =
        String.format(
            FILE_SYSTEM_URI_TEMPLATE,
            coordinate.getGroup(),
            coordinate.getName(),
            coordinate.getVersion());
    val uri = URI.create(uriValue);
    FileSystem fs = null;

    val allProviders = retrieveOrLoadCachedProviders(kernel, uri.getScheme());
    for (FileSystemProvider provider : allProviders) {
      synchronized (lock) {
        if (uri.getScheme().equals(provider.getScheme())) {
          try {
            fs = provider.getFileSystem(uri);
          } catch (FileSystemNotFoundException ex) {
            fs = provider.newFileSystem(uri, Collections.emptyMap());
          }
        }
        if (fs != null) {
          break;
        }
      }
    }

    if (fs == null) {
      try {
        fs = FileSystems.getFileSystem(uri);
      } catch (FileSystemNotFoundException | ProviderNotFoundException ex) {
        fs = FileSystems.newFileSystem(uri, Collections.emptyMap(), kernel.getClassLoader());
      }
    }
    return Pair.of(uriValue, fs);
  }

  static final AtomicReference<Set<FileSystemProvider>> existingProviders;

  static {
    existingProviders = new AtomicReference<>();
  }

  private static Set<FileSystemProvider> retrieveOrLoadCachedProviders(
      Kernel kernel, String scheme) {
    synchronized (lock) {
      val result = existingProviders.get();
      if (result == null) {
        existingProviders.set(load(kernel, scheme));
      }
      return existingProviders.get();
    }
  }

  private static Set<FileSystemProvider> load(Kernel kernel, String scheme) {

    Set<FileSystemProvider> results = Collections.newSetFromMap(new WeakHashMap<>());
    for (FileSystemProvider provider :
        ServiceLoader.load(FileSystemProvider.class, kernel.getClassLoader())) {
      if (scheme.equals(provider.getScheme())) {
        results.add(provider);
      }
    }
    return results;
  }

  public static ModuleClasspathManager moduleClasspathManager(
      DependencyGraph graph, ClassLoader classLoader, Kernel kernel) {
    val result =
        ServiceLoader.load(ModuleClasspathManagerProvider.class, classLoader)
            .findFirst()
            .get()
            .create(graph, kernel);
    return result;
  }

  static final Logger log = Logging.get(WritePluginDescriptorPhase.class);

  public static boolean performInstallation(
      Scope scope, Set<Module> installedPlugins, SunshowerKernel kernel) {
    handleKernelModules(scope, kernel);
    if (installedPlugins == null || installedPlugins.isEmpty()) {
      return true;
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
    return false;
  }

  private static void handleKernelModules(Scope scope, SunshowerKernel kernel) {
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

  private static void resolvePlugins(
      Kernel kernel, ModuleManager moduleManager, Set<Module> installedPlugins) {
    for (val module : installedPlugins) {
      val defaultModule = (AbstractModule) module;
      moduleManager.getModuleLoader().install(defaultModule);
      module.getLifecycle().setState(Lifecycle.State.Resolved);
      kernel.dispatchEvent(ModuleEvents.INSTALLED, Events.create(module));
    }
  }

  private static void checkForCyclicDependencies(
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

  private static void updateComponents(
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

  private static void handleCycle(
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

  private static void fireCyclicDependencyFailure(
      Kernel kernel,
      Coordinate coord,
      Component<DirectedGraph.Edge<Coordinate>, Coordinate> cycle) {
    kernel.dispatchEvent(
        ModuleEvents.INSTALL_FAILED,
        Events.create(new DependencyGraph.CyclicDependencySet(coord, cycle)));
  }

  private static void checkClose(CyclicDependencyException ex, Module plugin) {
    val fs = plugin.getFileSystem();
    if (fs != null) {
      try {
        fs.close();
      } catch (Exception x) {
        ex.addSuppressed(x);
      }
    }
  }

  private static void checkForUnresolvedDependencies(
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

  private static void fireUnresolvedDependencies(
      Kernel kernel, Set<DependencyGraph.UnsatisfiedDependencySet> unsatisfied) {
    for (val unsatisfiedDependencySet : unsatisfied) {
      kernel.dispatchEvent(ModuleEvents.INSTALL_FAILED, Events.create(unsatisfiedDependencySet));
    }
  }

  private static void saveAll(SunshowerKernel kernel, Set<Module> installedPlugins) {
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
