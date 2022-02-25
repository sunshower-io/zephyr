package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;

public interface ModuleLoader extends AutoCloseable {

  ModuleClasspath loadModule(Coordinate coordinate);
}
