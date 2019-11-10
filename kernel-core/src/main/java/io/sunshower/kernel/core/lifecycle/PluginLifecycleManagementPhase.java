package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;

public class PluginLifecycleManagementPhase extends Task {
  public PluginLifecycleManagementPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    return null;
  }
}
