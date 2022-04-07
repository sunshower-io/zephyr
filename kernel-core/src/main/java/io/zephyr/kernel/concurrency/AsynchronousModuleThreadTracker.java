package io.zephyr.kernel.concurrency;

import io.sunshower.lang.events.*;
import io.sunshower.lang.events.EventListener;
import io.zephyr.api.ModuleEvents;
import io.zephyr.api.ModuleTracker;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import java.util.function.Predicate;
import lombok.val;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.AvoidInstantiatingObjectsInLoops",
})
public class AsynchronousModuleThreadTracker extends AbstractAsynchronousObjectTracker<Module>
    implements ModuleTracker, EventListener<Module> {

  public AsynchronousModuleThreadTracker(
      Kernel kernel, Module host, ModuleThread taskQueue, Predicate<Module> filter) {
    super(kernel, host, taskQueue, filter);
  }

  @Override
  protected Runnable createExistingObjectDispatcher() {
    return new ExistingModuleDispatchTask();
  }

  final class ExistingModuleDispatchTask implements Runnable {

    @Override
    public void run() {
      val modules = kernel.getModuleManager().getModules();
      for (val module : modules) {
        val lifecycle = module.getLifecycle();
        val state = lifecycle.getState();
        if (state == Lifecycle.State.Installed) {
          taskQueue.schedule(
              new FilteredObjectDispatchTask(ModuleEvents.INSTALLED, Events.create(module)));
        } else if (state == Lifecycle.State.Active) {
          taskQueue.schedule(
              new FilteredObjectDispatchTask(ModuleEvents.INSTALLED, Events.create(module)));
          taskQueue.schedule(
              new FilteredObjectDispatchTask(ModuleEvents.STARTED, Events.create(module)));
        }
      }
    }
  }
}
