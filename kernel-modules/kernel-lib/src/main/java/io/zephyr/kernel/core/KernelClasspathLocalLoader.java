package io.zephyr.kernel.core;

import static io.zephyr.kernel.core.ModulePackageConstraintSet.canReexportPackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import lombok.extern.java.Log;
import lombok.val;
import org.jboss.modules.LocalLoader;
import org.jboss.modules.Resource;
import org.jboss.modules.ResourceLoader;

@Log
public class KernelClasspathLocalLoader implements LocalLoader, AutoCloseable {

  final ClassLoader classLoader;
  final List<ResourceLoader> strategies;
  final ThreadLocal<ModulePackageConstraintSet> moduleOverriddenPackageConstraintSet;

  public KernelClasspathLocalLoader(final Kernel kernel) {
    this.strategies = new ArrayList<>();
    this.classLoader = kernel.getClassLoader();
    this.moduleOverriddenPackageConstraintSet = new ThreadLocal<>();
    initialize();
  }

  @Override
  public Class<?> loadClassLocal(String name, boolean resolve) {
    try {
      val type = Class.forName(name, true, classLoader);
      if (canReexportPackage(type.getPackageName())) {
        return type;
      } else {
        log.log(
            Level.INFO,
            String.format(
                "Found, but not loading external class ('%s')--not in Zephyr public API", name));
        return null;
      }
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private synchronized boolean canReexportPackage(String packageName) {
    val result = ModulePackageConstraintSet.canReexportPackage(packageName, classLoader);
    if(result) {
      return result;
    }
    var cached = moduleOverriddenPackageConstraintSet.get();
    if(cached == null) {
      val threadClassLoader = Thread.currentThread().getContextClassLoader();
      if(threadClassLoader != null) {
        cached = new ModulePackageConstraintSet(threadClassLoader);
        moduleOverriddenPackageConstraintSet.set(cached);
      }
    }
    if(cached != null) {
      return cached.canReexport(packageName);
    }
    return false;
  }

  @Override
  public Package loadPackageLocal(String name) {
    return classLoader.getDefinedPackage(name);
  }

  @Override
  public List<Resource> loadResourceLocal(String name) {
    try {
      val resources = classLoader.getResources(name);
      val results = new ArrayList<Resource>();
      while (resources.hasMoreElements()) {
        results.add(new URLResource(resources.nextElement()));
      }
      loadExternal(name, results);
      return results;
    } catch (IOException ex) {
      return Collections.emptyList();
    }
  }

  private void loadExternal(String name, List<Resource> results) {
    for (val strategy : strategies) {
      val resource = strategy.getResource(name);
      if (resource != null) {
        results.add(resource);
      }
    }
  }

  private void initialize() {
    val loaders = ServiceLoader.load(ResourceLoadingStrategy.class, classLoader).iterator();
    while (loaders.hasNext()) {
      val loader = loaders.next().resourceLoader(classLoader);
      log.log(Level.INFO, "findmodule.registering.resourceloader", loader);
      strategies.add(loader);
      log.log(Level.INFO, "findmodule.registered.resourceloader", loader);
    }
  }

  @Override
  public void close() throws Exception {
    strategies.clear();
  }
}
