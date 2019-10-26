package io.sunshower.kernel.core;

import org.jboss.modules.IterableModuleFinder;
import org.jboss.modules.LocalLoader;
import org.jboss.modules.Resource;

import java.util.Collections;
import java.util.List;

public class KernelClasspathLocalLoader implements LocalLoader {

  final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

  @Override
  public Class<?> loadClassLocal(String name, boolean resolve) {
    try {
      return Class.forName(name, true, classLoader);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Override
  public Package loadPackageLocal(String name) {
    return classLoader.getDefinedPackage(name);
  }

  @Override
  public List<Resource> loadResourceLocal(String name) {
    return Collections.emptyList();
  }
}
