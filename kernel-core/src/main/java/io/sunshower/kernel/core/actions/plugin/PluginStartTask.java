package io.sunshower.kernel.core.actions.plugin;

import io.sunshower.PluginActivator;
import io.sunshower.gyre.Scope;
import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.ModuleManager;
import lombok.val;

public class PluginStartTask extends Task {
  private final Kernel kernel;
  private final Coordinate coordinate;
  private final ModuleManager manager;

  public PluginStartTask(Coordinate coordinate, ModuleManager manager, Kernel kernel) {
    super("plugin:start:" + coordinate.toCanonicalForm());
    this.manager = manager;
    this.kernel = kernel;
    this.coordinate = coordinate;
  }

  @Override
  public TaskValue run(Scope scope) {
    System.out.println("Starting...");
    val module = manager.getModule(coordinate);
    val currentState = module.getLifecycle().getState();
    if (!currentState.isAtLeast(Lifecycle.State.Active)) {
      val loader = module.getModuleClasspath().resolveServiceLoader(PluginActivator.class);
      for (val activator : loader) {
        activator.start(kernel);
      }
    }
    return null;
  }
}
