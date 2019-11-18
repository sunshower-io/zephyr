package io.zephyr.kernel.module;

import io.zephyr.kernel.Coordinate;

public interface ModuleRequest {
  Coordinate getCoordinate();

  ModuleLifecycle.Actions getLifecycleActions();
}
