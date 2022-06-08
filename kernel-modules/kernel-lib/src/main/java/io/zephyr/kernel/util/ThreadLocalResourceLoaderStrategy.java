package io.zephyr.kernel.util;

import io.zephyr.kernel.core.ResourceLoadingStrategy;
import io.zephyr.kernel.core.URLResource;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import lombok.val;
import org.jboss.modules.ClassSpec;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.PackageSpec;
import org.jboss.modules.Resource;
import org.jboss.modules.ResourceLoader;

public class ThreadLocalResourceLoaderStrategy implements ResourceLoadingStrategy, ResourceLoader {

  private static ModuleClassLoader classloader() {
    val classloader = Thread.currentThread().getContextClassLoader();
    if (classloader instanceof ModuleClassLoader) {
      return (ModuleClassLoader) classloader;
    }
    return null;
  }

  @Override
  public ResourceLoader resourceLoader(ClassLoader classLoader) {
    return this;
  }

  @Override
  public ClassSpec getClassSpec(String fileName) throws IOException {
    return null;
  }

  @Override
  public PackageSpec getPackageSpec(String name) throws IOException {
    return null;
  }

  @Override
  public Resource getResource(String name) {
    val cl = classloader();
    if (cl != null) {
      val url = cl.getResource(name);
      if (url != null) {
        return new URLResource(url);
      }
    }
    return null;
  }

  @Override
  public String getLibrary(String name) {
    return null;
  }

  @Override
  public Collection<String> getPaths() {
    val cl = classloader();
    if (cl != null) {
      return cl.getLocalPaths();
    }
    return Collections.emptySet();
  }
}
