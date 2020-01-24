package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.ModuleManager;
import lombok.val;

public class PluginStopTask extends Task {

  private final Coordinate coordinate;
  private final ModuleManager manager;

  public PluginStopTask(Coordinate coordinate, ModuleManager manager) {
    super("plugin:stop" + coordinate.toCanonicalForm());
    this.manager = manager;
    this.coordinate = coordinate;
  }

  @Override
  public TaskValue run(Scope scope) {
    val module = manager.getModule(coordinate);
    scope.set(PluginRemoveTask.MODULE_COORDINATE, coordinate);
    val taskQueue = module.getTaskQueue();
    if (taskQueue != null) { // may not have been started yet or may not be startable
      taskQueue.stop();
    }
    return null;
  }
}
