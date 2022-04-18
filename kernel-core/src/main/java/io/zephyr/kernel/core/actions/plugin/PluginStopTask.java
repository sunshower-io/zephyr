package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle.State;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.ModuleManager;
import io.zephyr.kernel.core.Modules;
import lombok.val;

public class PluginStopTask extends Task implements ModuleLifecycleTask {

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
    try {
      Modules.close(module);
    } catch (Exception ex) {
      module.getLifecycle().setState(State.Failed);
    }
    return null;
  }

  @Override
  public Coordinate getCoordinate() {
    return coordinate;
  }
}
