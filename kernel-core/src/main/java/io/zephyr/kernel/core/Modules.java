package io.zephyr.kernel.core;

import io.sunshower.gyre.Component;
import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Pair;
import io.sunshower.gyre.Scope;
import io.sunshower.lang.events.Events;
import io.zephyr.api.ModuleEvents;
import io.zephyr.common.io.Files;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.ModuleThread;
import io.zephyr.kernel.core.actions.ModuleInstallationCompletionPhase;
import io.zephyr.kernel.core.actions.ModulePhaseEvents;
import io.zephyr.kernel.core.actions.WritePluginDescriptorPhase;
import io.zephyr.kernel.dependencies.CyclicDependencyException;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.dependencies.UnresolvedDependencyException;
import io.zephyr.kernel.log.Logging;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings({
  "PMD.UnusedPrivateMethod",
  "PMD.UnusedFormalParameter",
  "PMD.DataflowAnomalyAnalysis"
})
public class Modules {

  static final String FILE_SYSTEM_URI_TEMPLATE = "droplet://%s.%s?version=%s";
  static final Object lock = new Object();
  static final AtomicReference<Set<FileSystemProvider>> existingProviders;
  static final Logger log = Logging.get(WritePluginDescriptorPhase.class);

  static {
    existingProviders = new AtomicReference<>();
  }

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

  public static boolean performInstallation(
      Scope scope, Set<Module> installedPlugins, SunshowerKernel kernel) {
    handleKernelModules(scope, kernel);
    if (installedPlugins == null || installedPlugins.isEmpty()) {
      return true;
    }

    for (val installedPlugin : installedPlugins) {
      if (installedPlugin == null) {
        throw new IllegalStateException("Error: null module somehow");
      }
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
      Kernel kernel, DependencyGraph dependencyGraph, Collection<Module> installedModules) {

    //    val results = dependencyGraph.getUnresolvedDependencies(installedModules);
    val results = dependencyGraph.resolveDependencies(installedModules);
    val unsatisfied = new LinkedHashSet<DependencyGraph.UnsatisfiedDependencySet>();
    for (val unresolvedDependency : results) {

      if (!unresolvedDependency.isSatisfied()) {
        val module = unresolvedDependency.getSource();
        for (val installedModule : installedModules) {
          if (module.equals(installedModule.getCoordinate())) {
            try {
              val fs = installedModule.getFileSystem();
              if (fs != null) {
                fs.close();
              }
            } catch (Exception ex) {
              log.log(Level.WARNING, "failed to close module filesystem {0}", module);
            }
          }
        }
        unsatisfied.add(unresolvedDependency);
      }
    }

    if (!unsatisfied.isEmpty()) {
      fireUnresolvedDependencies(kernel, unsatisfied);
      log.warning("plugin.phase.unresolveddependencies");

      log.log(Level.WARNING, "existing modules: ");
      val modules = kernel.getModuleManager().getModules();
      for (val module : modules) {
        log.log(Level.WARNING, "Module: {0}", module.getCoordinate());
        log.log(Level.WARNING, "\t State: {0}", module.getLifecycle().getState());
        val directories = module.getFileSystem().getRootDirectories();
        for (val directory : directories) {
          log.log(Level.WARNING, "\t\t root directory: {0}", directory);
        }
      }
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

  public static void close(Module module, Kernel kernel) throws Exception {
    val taskQueue = module.getTaskQueue();
    if (taskQueue != null) {
      taskQueue.stop();
    }
    module.close();
    kernel.getModuleManager().getModuleLoader().uninstall(module);
    System.gc();
  }

  public static void start(Module toStart, Kernel kernel) throws IOException {
    val module = (DefaultModule) toStart;
    val taskQueue = new ModuleThread(module, kernel);
    module.setTaskQueue(taskQueue);
    taskQueue.start();
    kernel.getModuleManager().getModuleLoader().install(module);
    module.setFileSystem(getFileSystem(module.getCoordinate(), kernel).snd);
  }
}
