package io.zephyr.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.concurrency.Task;

public class PluginLifecycleManagementPhase extends Task {
  public PluginLifecycleManagementPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    return null;
  }
}
