package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.PluginException;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.DefaultModule;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import java.io.IOException;
import lombok.val;

public class PluginStopTask extends Task {

  private final Kernel kernel;
  private final Coordinate coordinate;
  private final ModuleManager manager;

  public PluginStopTask(Coordinate coordinate, ModuleManager manager, Kernel kernel) {
    super("plugin:stop" + coordinate.toCanonicalForm());
    this.kernel = kernel;
    this.manager = manager;
    this.coordinate = coordinate;
  }

  @Override
  public TaskValue run(Scope scope) {
    synchronized (this) {
      val module = manager.getModule(coordinate);
      val currentState = module.getLifecycle().getState();
      scope.set(PluginRemoveTask.MODULE_COORDINATE, coordinate);
      if (currentState == Lifecycle.State.Resolved) {
        try {
          module.getFileSystem().close();
        } catch (IOException ex) {
          module.getLifecycle().setState(Lifecycle.State.Failed);
          throw new PluginException(ex);
        }
      }
      if (currentState == Lifecycle.State.Active) { // // TODO: 11/11/19 handle Failed
        try {
          module.getLifecycle().setState(Lifecycle.State.Stopping);
          module.getActivator().stop(kernel, module);
          ((DefaultModule) module).setActivator(null);
          try {
            module.getFileSystem().close();
          } catch (Exception ex) {
            module.getLifecycle().setState(Lifecycle.State.Failed);
            throw new PluginException(ex);
          }
        } finally {
          if (module.getLifecycle().getState() != Lifecycle.State.Failed) {
            module.getLifecycle().setState(Lifecycle.State.Resolved);
          }
        }
      }

      return null;
    }
  }
}
