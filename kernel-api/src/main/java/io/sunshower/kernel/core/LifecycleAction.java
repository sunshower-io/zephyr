package io.sunshower.kernel.core;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;

public interface LifecycleAction {
  Module getTarget();

  Lifecycle.State getState();

  ActionTree getActionTree();
}
