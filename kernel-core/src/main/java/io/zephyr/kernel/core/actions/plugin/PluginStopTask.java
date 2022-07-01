package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle.State;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import io.zephyr.kernel.core.Modules;
import lombok.val;

public class PluginStopTask extends Task implements ModuleLifecycleTask {

  private final Coordinate coordinate;
  private final ModuleManager manager;
  private final Kernel kernel;

  public PluginStopTask(Coordinate coordinate, ModuleManager manager, Kernel kernel) {
    super("plugin:stop" + coordinate.toCanonicalForm());
    this.kernel = kernel;
    this.manager = manager;
    this.coordinate = coordinate;
  }

  @Override
  public TaskValue run(Scope scope) {
    if (!coordinate.isResolved()) {
      return null;
    }
    val module = manager.getModule(coordinate);
    scope.set(PluginRemoveTask.MODULE_COORDINATE, coordinate);
    try {
      Modules.close(module, kernel);
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
