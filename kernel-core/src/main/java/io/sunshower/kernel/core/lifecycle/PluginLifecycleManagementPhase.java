package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.concurrency.Context;
import io.sunshower.kernel.concurrency.Task;

public class PluginLifecycleManagementPhase extends Task {
  public PluginLifecycleManagementPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Context context) {
    return null;
  }
}
