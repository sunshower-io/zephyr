package io.sunshower.kernel.test;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.core.ModuleClasspath;
import io.zephyr.kernel.core.ModuleLoader;

public class SimulatedModuleLoader implements ModuleLoader {

  final ModuleClasspath classpath;

  public SimulatedModuleLoader() {
    classpath = new SimulatedModuleClasspath(this);
  }

  @Override
  public ModuleClasspath loadModule(Coordinate coordinate) {
    return classpath;
  }
}
