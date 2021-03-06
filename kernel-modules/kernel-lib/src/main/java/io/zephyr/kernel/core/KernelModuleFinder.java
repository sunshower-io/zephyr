package io.zephyr.kernel.core;

import io.zephyr.kernel.Library;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.log.Logging;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.val;
import org.jboss.modules.*;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.filter.PathFilters;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public final class KernelModuleFinder implements ModuleFinder {

  static final Logger log = Logging.get(KernelModuleFinder.class, "ModuleClassloading");
  private final Module module;
  private final ModuleLoader moduleLoader;

  private final LocalLoader localLoader;

  KernelModuleFinder(
      @NonNull Module module, @NonNull final ModuleLoader loader, @NonNull Kernel kernel) {
    this.module = module;
    this.moduleLoader = loader;
    this.localLoader = new KernelClasspathLocalLoader(kernel);
  }

  @Override
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public ModuleSpec findModule(String name, ModuleLoader delegateLoader)
      throws ModuleLoadException {
    val coordinate = module.getCoordinate();
    val identifier = coordinate.toCanonicalForm();
    log.log(Level.FINE, "findmodule.entry", new Object[] {name, identifier});

    if (name == null) {
      log.log(Level.INFO, "");
      return null;
    }

    val normalized = name.trim();

    if (!normalized.equals(identifier)) {
      log.log(Level.INFO, "findmodule.name.notthis", new Object[] {name, identifier});
      return null;
    }

    val assembly = module.getAssembly();

    if (assembly == null) {
      log.log(Level.INFO, "findmodule.fallthrough", name);
      return ModuleSpec.build(normalized).setFallbackLoader(localLoader).create();
    }

    val assemblyFile = assembly.getFile();
    if (assemblyFile == null) {
      log.log(Level.INFO, "findmodule.fallthrough", name);
      return ModuleSpec.build(normalized).setFallbackLoader(localLoader).create();
    }

    val moduleSpec = ModuleSpec.build(identifier);

    try {
      createRootResource(moduleSpec, assemblyFile, assembly.getSubpaths());
      defineLibraries(moduleSpec, module.getLibraries());
    } catch (IOException ex) {
      throw new ModuleLoadException(ex);
    }

    moduleSpec.addDependency(DependencySpec.OWN_DEPENDENCY);
    val dependencies = module.getDependencies();
    for (val dependency : dependencies) {
      val dep =
          new ModuleDependencySpecBuilder()
              .setName(dependency.getCoordinate().toCanonicalForm())
              .setModuleLoader(moduleLoader)
              .setImportServices(true)
              .setExport(true)
              .setImportFilter(PathFilters.acceptAll())
              .build();
      moduleSpec.addDependency(dep);
    }

    moduleSpec.setFallbackLoader(localLoader);

    return moduleSpec.create();
  }

  private void defineLibraries(ModuleSpec.Builder moduleSpec, Set<Library> libraries)
      throws IOException {
    for (val lib : libraries) {
      createRootResource(moduleSpec, lib.getFile());
    }
  }

  private void createRootResource(ModuleSpec.Builder spec, File file, Set<String> paths)
      throws IOException {
    createRootResource(spec, file);
    if (paths == null) {
      return;
    }
    for (val path : paths) {
      createRootResource(spec, file, path);
    }
  }

  private void createRootResource(ModuleSpec.Builder spec, File file, String name)
      throws IOException {
    val loader = ResourceLoaders.createJarResourceLoader(new JarFile(file), name);
    val loaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(loader);
    spec.addResourceRoot(loaderSpec);
  }

  private void createRootResource(ModuleSpec.Builder spec, File file) throws IOException {
    val loader = ResourceLoaders.createJarResourceLoader(new JarFile(file));
    val loaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(loader);
    spec.addResourceRoot(loaderSpec);
  }
}
