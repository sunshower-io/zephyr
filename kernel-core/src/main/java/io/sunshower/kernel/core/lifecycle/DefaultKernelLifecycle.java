package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.concurrency.*;
import io.sunshower.kernel.concurrency.Process;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.KernelLifecycle;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import javax.inject.Inject;

import io.sunshower.kernel.module.ModuleLifecycle;
import io.sunshower.kernel.module.ModuleLifecycleChangeGroup;
import io.sunshower.kernel.module.ModuleLifecycleChangeRequest;
import lombok.val;

import java.util.concurrent.CompletionStage;

import static io.sunshower.kernel.core.lifecycle.DefaultKernelLifecycle.LifecycleProcessHolder.stopInstance;
import static io.sunshower.kernel.core.lifecycle.DefaultKernelLifecycle.LifecycleProcessHolder.stopPlugins;

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
  public CompletionStage<Process<String>> stop() {
    return scheduler.submit(stopPlugins(kernel)).thenCompose(this::doStop);
  }

  private TaskTracker<String> doStop(Process<String> taskSets) {
    return scheduler.submit(stopInstance(kernel));
  }

  @Override
  public CompletionStage<Process<String>> start() {
    return scheduler.submit(LifecycleProcessHolder.startInstance(kernel));
  }

  @Override
  public CompletionStage<Process<String>> setState(State state) {
    return null;
  }

  static final class LifecycleProcessHolder {

    static final Process<String> stopInstance(Kernel kernel) {
      val scope = Scope.root();
      scope.set("SunshowerKernel", kernel);
      return Tasks.newProcess("kernel:stop")
          .withContext(scope)
          .register(new UnloadKernelFilesystemPhase("kernel:stop:filesystem"))
          .register(new UnloadKernelClassloaderPhase("kernel:stop:classloader"))
          .task("kernel:stop:classloader")
          .dependsOn("kernel:stop:filesystem")
          .create();
    }

    static final Process<String> startInstance(Kernel kernel) {
      val scope = Scope.root();
      scope.set("SunshowerKernel", kernel);
      return Tasks.newProcess("kernel:start:filesystem")
          .withContext(scope)
          .register(new KernelModuleListReadPhase("kernel:lifecycle:module:list"))
          .register(new KernelFilesystemCreatePhase("kernel:lifecycle:filesystem:create"))
          .register(new KernelClassLoaderCreationPhase("kernel:lifecycle:classloader"))
          .task("kernel:lifecycle:module:list")
          .dependsOn("kernel:lifecycle:filesystem:create")
          .task("kernel:lifecycle:classloader")
          .dependsOn("kernel:lifecycle:module:list")
          .create();
    }

    public static Process<String> stopPlugins(SunshowerKernel kernel) {

      val running = kernel.getModuleManager().getModules();
      val results = new ModuleLifecycleChangeRequest[running.size()];
      int i = 0;
      for (val r : running) {
        results[i++] =
            new ModuleLifecycleChangeRequest(r.getCoordinate(), ModuleLifecycle.Actions.Stop);
      }
      return kernel
          .getModuleManager()
          .prepare(new ModuleLifecycleChangeGroup(results))
          .getProcess();
    }
  }
}
