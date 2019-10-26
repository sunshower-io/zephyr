package io.sunshower.kernel.lifecycle;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.DefaultModule;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.kernel.dependencies.DependencyGraph;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleNotFoundException;

@SuppressWarnings("PMD.AvoidUsingVolatile")
public class KernelModuleLoader extends ModuleLoader {

  private final Object lock = new Object();

  private volatile DependencyGraph graph;
  private final Map<String, UnloadableKernelModuleLoader> moduleLoaders;

  public KernelModuleLoader(final DependencyGraph graph) {
    moduleLoaders = new HashMap<>();
    this.graph = graph;
  }

  public void install(Module module) {
    synchronized (lock) {
      graph = graph.add(module);
      val coordinate = module.getCoordinate();
      val id = coordinate.toCanonicalForm();
      val loader = new UnloadableKernelModuleLoader(new KernelModuleFinder(module, this));
      moduleLoaders.put(id, loader);
    }
  }

  @SneakyThrows
  public void uninstall(Coordinate coordinate) {
    synchronized (lock) {
      val id = coordinate.toCanonicalForm();
      val loader = moduleLoaders.get(id);
      loader.unload(coordinate);
      moduleLoaders.remove(id);
      graph = graph.remove(coordinate);
    }
  }

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
      result = preloadModule(name, loader);
    }
    val target = (DefaultModule) graph.get(ModuleCoordinate.parse(name));
    target.setModule(result);
    return result;
  }

  final class UnloadableKernelModuleLoader extends ModuleLoader {

    final KernelModuleLoader loader;

    UnloadableKernelModuleLoader(KernelModuleFinder kernelModuleFinder) {
      super(kernelModuleFinder);
      loader = KernelModuleLoader.this;
    }

    boolean unload(Coordinate coordinate) throws ModuleLoadException {
      val id = coordinate.toCanonicalForm();
      val dependants = graph.getDependants(coordinate);

      for (val dependant : dependants) {
        val typedDep = (DefaultModule) dependant;
        val actualModule = typedDep.getModule();
        val actualModuleLoader = (UnloadableKernelModuleLoader) actualModule.getModuleLoader();
        actualModuleLoader.loader.uninstall(dependant.getCoordinate());
      }

      val module = findLoadedModuleLocal(id);
      val result = unloadModuleLocal(id, module);
      refreshResourceLoaders(module);
      relink(module);

      return result;
    }
  }
}
