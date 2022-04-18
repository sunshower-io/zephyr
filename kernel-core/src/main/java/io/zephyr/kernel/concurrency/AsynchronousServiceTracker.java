package io.zephyr.kernel.concurrency;

import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.Events;
import io.zephyr.api.ServiceEvents;
import io.zephyr.api.ServiceReference;
import io.zephyr.api.ServiceTracker;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.TaskQueue;
import io.zephyr.kernel.core.Kernel;
import java.util.function.Predicate;
import lombok.val;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
})
public class AsynchronousServiceTracker
    extends AbstractAsynchronousObjectTracker<ServiceReference<?>>
    implements ServiceTracker, EventListener<ServiceReference<?>> {

  public AsynchronousServiceTracker(
      Kernel kernel, Module host, TaskQueue taskQueue, Predicate<ServiceReference<?>> filter) {
    super(kernel, host, taskQueue, filter);
  }

  @Override
  protected Runnable createExistingObjectDispatcher() {
    return new ExistingModuleScanningDispatcher();
  }

  class ExistingModuleScanningDispatcher implements Runnable {

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void run() {
      val activeModules = kernel.getModuleManager().getModules(Lifecycle.State.Active);

      for (val module : activeModules) {
        taskQueue.schedule(new ModuleServiceScanTask(module));
      }
    }
  }

  class ModuleServiceScanTask implements Runnable {
    final Module module;

    ModuleServiceScanTask(Module module) {
      this.module = module;
    }

    @Override
    public void run() {
      val registrations = kernel.getServiceRegistry().getRegistrations(module);
      if (registrations != null) {
        for (val registration : registrations) {
          onEvent(ServiceEvents.REGISTERED, Events.create(registration.getReference()));
        }
      }
    }
  }
}
