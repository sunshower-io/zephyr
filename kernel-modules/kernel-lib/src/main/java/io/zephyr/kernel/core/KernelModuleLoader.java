package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.ModuleException;
import io.zephyr.kernel.UnsatisfiedDependencyException;
import io.zephyr.kernel.dependencies.DependencyGraph;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.val;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleNotFoundException;

/** this class is intentionally not thread-safe and must be protected by its owner */
@SuppressWarnings("PMD.AvoidUsingVolatile")
@Log
public final class KernelModuleLoader extends ModuleLoader
    implements io.zephyr.kernel.core.ModuleLoader, ModuleClasspathManager, AutoCloseable {

  private final Kernel kernel;
  private DependencyGraph graph;
  private final Map<String, UnloadableKernelModuleLoader> moduleLoaders;

  public KernelModuleLoader(final DependencyGraph graph, Kernel kernel) {
    moduleLoaders = new HashMap<>();
    this.graph = graph;
    this.kernel = kernel;
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public void install(Module module) {
    val coordinate = module.getCoordinate();
    val id = coordinate.toCanonicalForm();
    if (module instanceof AbstractModule) {
      val loader = new UnloadableKernelModuleLoader(new KernelModuleFinder(module, this, kernel));
      ((AbstractModule) module).setModuleLoader(loader);
      moduleLoaders.put(id, loader);
    }
  }

  @Override
  @SneakyThrows
  public void uninstall(Coordinate coordinate) {
    val id = coordinate.toCanonicalForm();
    val loader = moduleLoaders.get(id);
    if (loader != null) {
      loader.unload(coordinate);
      moduleLoaders.remove(id);
    }
  }

  @Override
  public void uninstall(@NonNull Module module) {
    uninstall(module.getCoordinate());
  }

  @Override
  public void check(Module module) {
    val coord = module.getCoordinate();
    val canonicalForm = coord.toCanonicalForm();
    if (!moduleLoaders.containsKey(canonicalForm)) {
      install(module);
    } else {
      val loader = moduleLoaders.get(canonicalForm);
      ((AbstractModule) module).setModuleLoader(loader);
    }
  }

  @Override
  protected org.jboss.modules.Module preloadModule(final String name) throws ModuleLoadException {

    org.jboss.modules.Module result = loadModuleLocal(name);
    if (result == null) {
      val loader = moduleLoaders.get(name);
      if (loader == null) {
        throw new ModuleNotFoundException("Module identified by " + name + " was not found");
      }
      result = ModuleLoader.preloadModule(name, loader);
    }
    val target = (AbstractModule) graph.get(ModuleCoordinate.parse(name));
    val loader = new UnloadableKernelModuleLoader(new KernelModuleFinder(target, this, kernel));
    val classpath = new DefaultModuleClasspath(result, loader);
    target.setModuleLoader(loader);
    target.setModuleClasspath(classpath);
    return result;
  }

  @Override
  public ModuleClasspath loadModule(Coordinate coordinate) {
    try {
      return new DefaultModuleClasspath(loadModule(coordinate.toCanonicalForm()), this);
    } catch (ModuleLoadException ex) {
      throw new ModuleException(ex);
    }
  }

  @Override
  public void close() throws Exception {
    for (val loader : moduleLoaders.entrySet()) {
      try {
        loader.getValue().close();
      } catch (Throwable ex) {
        log.log(
            Level.WARNING,
            "Failed to close loader for module {0}, reason: {1}",
            new Object[] {loader.getKey(), ex.getMessage()});
      }
    }
  }

  final class UnloadableKernelModuleLoader extends ModuleLoader
      implements io.zephyr.kernel.core.ModuleLoader, AutoCloseable {

    final KernelModuleLoader loader;
    private Coordinate coordinate;

    UnloadableKernelModuleLoader(KernelModuleFinder kernelModuleFinder) {
      super(kernelModuleFinder);
      loader = KernelModuleLoader.this;
    }

    boolean unload(Coordinate coordinate) throws ModuleLoadException {
      val id = coordinate.toCanonicalForm();
      // parallel unloading can be hard to reason about, but one of the invariants
      // is that the modules are always shut down in the correct order even if one has been shut
      // down
      // as part of the dependent graph of another
      val module = findLoadedModuleLocal(id);

      if (module != null) {
        val result = unloadModuleLocal(id, module);
        refreshResourceLoaders(module);
        setAndRelinkDependencies(module, Collections.emptyList());
        relink(module);
        return result;
      }
      return false;
    }

    @Override
    public ModuleClasspath loadModule(Coordinate coordinate) {

      try {
        this.coordinate = coordinate;
        return new DefaultModuleClasspath(loadModule(coordinate.toCanonicalForm()), this);
      } catch (ModuleLoadException e) {
        throw new UnsatisfiedDependencyException(e);
      }
    }

    public void close() throws Exception {
      if (coordinate != null) {
        unload(coordinate);
      }
      coordinate = null;
    }
  }
}
