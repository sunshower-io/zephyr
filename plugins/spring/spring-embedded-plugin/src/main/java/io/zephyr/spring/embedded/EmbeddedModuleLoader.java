package io.zephyr.spring.embedded;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.core.ModuleClasspath;
import io.zephyr.kernel.core.ModuleLoader;

public class EmbeddedModuleLoader implements ModuleLoader {

  final ModuleClasspath classpath;

  public EmbeddedModuleLoader(ClassLoader loader) {
    classpath = new EmbeddedModuleClasspath(this, loader);
  }

  @Override
  public ModuleClasspath loadModule(Coordinate coordinate) {
    return classpath;
  }

  @Override
  public void close() throws Exception {
    //nothing to do
  }
}
