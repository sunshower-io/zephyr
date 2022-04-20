package io.zephyr.kernel.core;

import io.zephyr.kernel.Dependency.ServicesResolutionStrategy;
import io.zephyr.kernel.Library;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.log.Logging;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.val;
import org.jboss.modules.DependencySpec;
import org.jboss.modules.LocalLoader;
import org.jboss.modules.ModuleDependencySpecBuilder;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.ClassFilter;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public final class KernelModuleFinder implements ModuleFinder, AutoCloseable {

  static final Logger log = Logging.get(KernelModuleFinder.class, "ModuleClassloading");
  private final Module module;
  private final ModuleLoader moduleLoader;
  private final List<ResourceLoader> resourceLoaders;

  private final LocalLoader localLoader;

  KernelModuleFinder(
      @NonNull Module module, @NonNull final ModuleLoader loader, @NonNull Kernel kernel) {
    this.module = module;
    this.moduleLoader = loader;
    this.localLoader = new KernelClasspathLocalLoader(kernel);
    this.resourceLoaders = new ArrayList<>(64);
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

    /** todo JOSIAH: add to dependency spec */
    moduleSpec.addDependency(DependencySpec.OWN_DEPENDENCY);
    val dependencies = module.getDependencies();
    for (val dependency : dependencies) {
      val dep =
          new ModuleDependencySpecBuilder()
              .setName(dependency.getCoordinate().toCanonicalForm())
              .setModuleLoader(moduleLoader)
              .setImportServices(
                  dependency.getServicesResolutionStrategy() == ServicesResolutionStrategy.Import)
              .setExport(dependency.isReexport())
              .setOptional(dependency.isOptional())
              .setExportFilter(toPathFilter(dependency.getExports()))
              .setImportFilter(toPathFilter(dependency.getImports()))
              .setClassImportFilter(toClassFilter(dependency.getImports()))
              .setClassExportFilter(toClassFilter(dependency.getExports()))
              .build();
      moduleSpec.addDependency(dep);
    }

    moduleSpec.setFallbackLoader(localLoader);

    return moduleSpec.create();
  }

  private ResourceLoader register(ResourceLoader loader) {
    resourceLoaders.add(loader);
    return loader;
  }

  private ClassFilter toClassFilter(List<PathSpecification> classes) {
    if (classes == null || classes.isEmpty()) {
      return ClassFilters.acceptAll();
    }

    val result = new ArrayList<ClassFilter>();
    for (val pathSpec : classes) {
      switch (pathSpec.getMode()) {
        case Class:
          result.add(ClassFilters.fromResourcePathFilter(PathFilters.is(pathSpec.getPath())));
          break;
      }
    }
    return className -> result.stream().anyMatch(filter -> filter.accept(className));
  }

  private PathFilter toPathFilter(List<PathSpecification> paths) {
    if (paths == null || paths.isEmpty()) {
      return PathFilters.acceptAll();
    }

    val result = new ArrayList<PathFilter>();
    for (val pathSpec : paths) {
      switch (pathSpec.getMode()) {
        case All:
          result.add(PathFilters.match(pathSpec.getPath()));
          break;
        case Just:
          result.add(PathFilters.in(Set.of(pathSpec.getPath())));
      }
    }
    return PathFilters.any(result);
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
    val loader = register(ResourceLoaders.createJarResourceLoader(new JarFile(file), name));
    val loaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(loader);
    spec.addResourceRoot(loaderSpec);
  }

  private void createRootResource(ModuleSpec.Builder spec, File file) throws IOException {
    val loader = register(ResourceLoaders.createJarResourceLoader(new JarFile(file)));
    val loaderSpec = ResourceLoaderSpec.createResourceLoaderSpec(loader);
    spec.addResourceRoot(loaderSpec);
  }

  @Override
  public void close() throws Exception {
    for (val resourceLoader : resourceLoaders) {
      resourceLoader.close();
    }
    ((KernelClasspathLocalLoader) localLoader).close();
  }
}
