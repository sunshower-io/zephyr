package io.sunshower.kernel.module;

import io.sunshower.kernel.Coordinate;

public interface ModuleRequest {
  Coordinate getCoordinate();

  ModuleLifecycle.Actions getLifecycleActions();
}
