package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.ModuleException;
import io.sunshower.kernel.UnsatisfiedDependencyException;
import io.sunshower.kernel.dependencies.DependencyGraph;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleNotFoundException;

/** this class is intentionally not thread-safe and must be protected by its owner */
@SuppressWarnings("PMD.AvoidUsingVolatile")
public final class KernelModuleLoader extends ModuleLoader
    implements io.sunshower.kernel.core.ModuleLoader, ModuleClasspathManager {

  private DependencyGraph graph;
  private final Map<String, UnloadableKernelModuleLoader> moduleLoaders;

  public KernelModuleLoader(final DependencyGraph graph) {
    moduleLoaders = new HashMap<>();
    this.graph = graph;
  }

  @Override
  public void install(Module module) {
    val coordinate = module.getCoordinate();
    val id = coordinate.toCanonicalForm();
    val loader = new UnloadableKernelModuleLoader(new KernelModuleFinder(module, this));
    ((DefaultModule) module).setModuleLoader(loader);
    moduleLoaders.put(id, loader);
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
  protected org.jboss.modules.Module preloadModule(final String name) throws ModuleLoadException {

    org.jboss.modules.Module result = loadModuleLocal(name);
    if (result == null) {
      val loader = moduleLoaders.get(name);
      if (loader == null) {
        throw new ModuleNotFoundException("Module identified by " + name + " was not found");
      }
      result = ModuleLoader.preloadModule(name, loader);
    }
    val target = (DefaultModule) graph.get(ModuleCoordinate.parse(name));
    val loader = new UnloadableKernelModuleLoader(new KernelModuleFinder(target, this));
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

  final class UnloadableKernelModuleLoader extends ModuleLoader
      implements io.sunshower.kernel.core.ModuleLoader {

    final KernelModuleLoader loader;

    UnloadableKernelModuleLoader(KernelModuleFinder kernelModuleFinder) {
      super(kernelModuleFinder);
      loader = KernelModuleLoader.this;
    }

    boolean unload(Coordinate coordinate) throws ModuleLoadException {
      val id = coordinate.toCanonicalForm();
      //parallel unloading can be hard to reason about, but one of the invariants
      // is that the modules are always shut down in the correct order even if one has been shut down
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
        return new DefaultModuleClasspath(loadModule(coordinate.toCanonicalForm()), this);
      } catch (ModuleLoadException e) {
        throw new UnsatisfiedDependencyException(e);
      }
    }
  }
}
