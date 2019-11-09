package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.concurrency.*;
import io.sunshower.kernel.concurrency.Process;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.KernelLifecycle;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import javax.inject.Inject;
import lombok.val;

@SuppressFBWarnings
@SuppressWarnings("PMD.UnusedPrivateField")
public class DefaultKernelLifecycle implements KernelLifecycle {

  private SunshowerKernel kernel;
  private Scheduler<String> scheduler;

  @Inject
  public DefaultKernelLifecycle(SunshowerKernel kernel, Scheduler<String> scheduler) {
    this.kernel = kernel;
    this.scheduler = scheduler;
  }

  @Override
  public State getState() {
    return null;
  }

  @Override
  public TaskTracker<String> stop() {
    return null;
  }

  @Override
  public TaskTracker<String> start() {
    return scheduler.submit(LifecycleProcessHolder.startInstance(kernel));
  }

  @Override
  public TaskTracker<String> setState(State state) {
    return null;
  }

  static final class LifecycleProcessHolder {

    static final Process<String> startInstance(Kernel kernel) {
      val scope = ReductionScope.newContext();
      scope.set("SunshowerKernel", kernel);
      return Tasks.newProcess("kernel:start:filesystem")
          .withContext(scope)
          .register("kernel:lifecycle:module:list", new KernelModuleListReadPhase())
          .register("kernel:lifecycle:filesystem:create", new KernelFilesystemCreatePhase())
          .register("kernel:lifecycle:classloader", new KernelClassLoaderCreationPhase())
          .task("kernel:lifecycle:module:list")
          .dependsOn("kernel:lifecycle:filesystem:create")
          .task("kernel:lifecycle:classloader")
          .dependsOn("kernel:lifecycle:module:list")
          .create();
    }
  }
}
