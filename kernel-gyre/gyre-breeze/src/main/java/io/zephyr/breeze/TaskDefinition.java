package io.zephyr.breeze;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.concurrency.Task;

final class TaskDefinition extends Task {

  TaskDefinition(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    return null;
  }
}
