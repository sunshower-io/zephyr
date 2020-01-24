package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;

public interface ModuleLoader {

  ModuleClasspath loadModule(Coordinate coordinate);
}
