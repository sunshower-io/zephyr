package io.sunshower.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.PluginException;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.core.DefaultModule;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.ModuleManager;
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
      if (currentState == Lifecycle.State.Resolved) {
        try {
          module.getFileSystem().close();
          kernel.getModuleClasspathManager().uninstall(module);
        } catch (IOException ex) {
          module.getLifecycle().setState(Lifecycle.State.Failed);
          throw new PluginException(ex);
        }
      }
      if (currentState == Lifecycle.State.Active) { // // TODO: 11/11/19 handle Failed
        module.getLifecycle().setState(Lifecycle.State.Stopping);
        module.getActivator().stop(kernel);
        kernel.getModuleClasspathManager().uninstall(module);
        ((DefaultModule) module).setActivator(null);
        try {
          module.getFileSystem().close();
        } catch (IOException ex) {
          module.getLifecycle().setState(Lifecycle.State.Failed);
          throw new PluginException(ex);
        }
        module.getLifecycle().setState(Lifecycle.State.Resolved);
      }

      return null;
    }
  }
}
