package io.zephyr.kernel.core.actions.plugin;

import io.sunshower.gyre.Scope;
import io.zephyr.PluginActivator;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.DefaultModule;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.status.Status;
import io.zephyr.kernel.status.StatusType;
import java.util.ServiceConfigurationError;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class PluginStartTask extends Task {
  private final Kernel kernel;
  private final Coordinate coordinate;
  private final ModuleManager manager;

  static final Logger log = Logger.getLogger(PluginStartTask.class.getName());

  static final String FAILURE_TEMPLATE = "Failed to start plugin ''{0}''.  Reason: ''{1}''";

  public PluginStartTask(Coordinate coordinate, ModuleManager manager, Kernel kernel) {
    super("plugin:start:" + coordinate.toCanonicalForm());
    this.manager = manager;
    this.kernel = kernel;
    this.coordinate = coordinate;
  }

  @Override
  @SuppressWarnings({
    "PMD.AvoidBranchingStatementAsLastInLoop",
    "PMD.DataflowAnomalyAnalysis",
    "PMD.AvoidInstantiatingObjectsInLoops"
  })
  public TaskValue run(Scope scope) {

    val module = manager.getModule(coordinate);
    kernel.dispatchEvent(
        ModuleEvents.STARTING,
        Events.create(module, Status.resolvable(StatusType.PROGRESSING, "Starting module...")));
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

          kernel.dispatchEvent(
              ModuleEvents.START_FAILED,
              Events.create(
                  module,
                  StatusType.FAILED.unresolvable(FAILURE_TEMPLATE, coordinate, ex.getMessage())));
          module.getLifecycle().setState(Lifecycle.State.Failed);
          log.log(Level.WARNING, FAILURE_TEMPLATE, new Object[] {coordinate, ex.getMessage()});
          log.log(Level.INFO, "Reason: ", ex);
          return null;
        }
      }
      kernel.dispatchEvent(
          ModuleEvents.STARTED,
          Events.create(
              module,
              Status.resolvable(
                  StatusType.PROGRESSING,
                  "Successfully started module " + module.getCoordinate())));

      module.getLifecycle().setState(Lifecycle.State.Active);
    }
    return null;
  }
}
