package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.PluginActivator;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.DefaultModule;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import java.util.ServiceConfigurationError;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class PluginStartTask extends Task {
  private final Kernel kernel;
  private final Coordinate coordinate;
  private final ModuleManager manager;

  static final Logger log = Logger.getLogger(PluginStartTask.class.getName());

  public PluginStartTask(Coordinate coordinate, ModuleManager manager, Kernel kernel) {
    super("plugin:start:" + coordinate.toCanonicalForm());
    this.manager = manager;
    this.kernel = kernel;
    this.coordinate = coordinate;
  }

  /**
   * synchronized so that lifecycle state propagates normally across threads. However, nobody should
   * be looking at the state while a plugin is starting--we need to listen for the relevant module
   * lifecycle events dispatched by the PluginContext
   */
  @Override
  @SuppressWarnings({"PMD.AvoidBranchingStatementAsLastInLoop", "PMD.DataflowAnomalyAnalysis"})
  public TaskValue run(Scope scope) {
    synchronized (this) {
      val module = manager.getModule(coordinate);
      val currentState = module.getLifecycle().getState();
      if (!currentState.isAtLeast(Lifecycle.State.Active)) { // // TODO: 11/11/19 handle failed
        module.getLifecycle().setState(Lifecycle.State.Starting);
        val loader = module.getModuleClasspath().resolveServiceLoader(PluginActivator.class);
        manager.getModuleLoader().check(module);
        val ctx = kernel.createContext(module);
        for (val activator : loader) {
          try {
            activator.start(ctx);
            ((DefaultModule) module).setActivator(activator);
          } catch (Exception | ServiceConfigurationError | LinkageError ex) {
            module.getLifecycle().setState(Lifecycle.State.Failed);
            log.log(
                Level.WARNING, "Failed to start plugin ''{0}''.  Reason: ''{1}''", ex.getMessage());
            log.log(Level.INFO, "Reason: ", ex);
            return null;
          }
          break;
        }
        module.getLifecycle().setState(Lifecycle.State.Active);
      }
      return null;
    }
  }
}
