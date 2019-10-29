package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;

public interface ModuleLoader {

  ModuleClasspath loadModule(Coordinate coordinate);
}
