package io.zephyr.kernel.core;

import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;

public interface LifecycleAction {
  Module getTarget();

  Lifecycle.State getState();
}
